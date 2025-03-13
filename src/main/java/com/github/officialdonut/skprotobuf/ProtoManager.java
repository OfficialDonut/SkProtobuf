package com.github.officialdonut.skprotobuf;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.TypeRegistry;
import com.google.protobuf.util.JsonFormat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

public class ProtoManager {

    private Map<String, DescriptorProtos.FileDescriptorProto> protos;
    private Map<String, Descriptors.FileDescriptor> fileDescriptors;
    private Map<String, Descriptors.Descriptor> messageDescriptors;
    private JsonFormat.Printer jsonPrinter;
    private JsonFormat.Parser jsonParser;
    private final Path descriptorDir;

    public ProtoManager(Path descriptorDir) {
        this.descriptorDir = descriptorDir;
        try {
            Files.createDirectories(descriptorDir);
        } catch (IOException e) {
            SkProtobuf.getInstance().getLogger().log(Level.SEVERE, "Failed to create descriptor directory", e);
        }
    }

    public void loadDescriptors() {
        protos = new HashMap<>();
        fileDescriptors = new HashMap<>();
        messageDescriptors = new HashMap<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(descriptorDir)) {
            for (Path path : stream) {
                try (InputStream is = Files.newInputStream(path)) {
                    for (DescriptorProtos.FileDescriptorProto proto : DescriptorProtos.FileDescriptorSet.parseFrom(is).getFileList()) {
                        protos.put(proto.getName(), proto);
                    }
                } catch (IOException e) {
                    SkProtobuf.getInstance().getLogger().log(Level.SEVERE, "Failed to load descriptors", e);
                }
            }
            protos.values().forEach(this::loadDescriptor);
            for (Descriptors.FileDescriptor fileDescriptor : fileDescriptors.values()) {
                for (Descriptors.Descriptor descriptor : fileDescriptor.getMessageTypes()) {
                    messageDescriptors.put(descriptor.getFullName(), descriptor);
                    messageDescriptors.putIfAbsent(descriptor.getName(), descriptor);
                }
            }
            TypeRegistry typeRegistry = TypeRegistry.newBuilder().add(new HashSet<>(messageDescriptors.values())).build();
            jsonPrinter = JsonFormat.printer().usingTypeRegistry(typeRegistry).preservingProtoFieldNames().omittingInsignificantWhitespace();
            jsonParser = JsonFormat.parser().usingTypeRegistry(typeRegistry);
        } catch (IOException e) {
            SkProtobuf.getInstance().getLogger().log(Level.SEVERE, "Failed to load descriptors", e);
        }
    }

    private Descriptors.FileDescriptor loadDescriptor(DescriptorProtos.FileDescriptorProto proto) {
        if (fileDescriptors.containsKey(proto.getName())) {
            return fileDescriptors.get(proto.getName());
        }
        List<Descriptors.FileDescriptor> dependencies = new ArrayList<>();
        for (String dependency : proto.getDependencyList()) {
            if (protos.containsKey(dependency)) {
                Descriptors.FileDescriptor descriptor = loadDescriptor((protos.get(dependency)));
                if (descriptor != null) {
                    dependencies.add(descriptor);
                }
            }
        }
        try {
            Descriptors.FileDescriptor descriptor = Descriptors.FileDescriptor.buildFrom(proto, dependencies.toArray(Descriptors.FileDescriptor[]::new));
            fileDescriptors.put(proto.getName(), descriptor);
            return descriptor;
        } catch (Descriptors.DescriptorValidationException e) {
            SkProtobuf.getInstance().getLogger().log(Level.SEVERE, "Failed to load descriptors", e);
            return null;
        }
    }

    public Set<Descriptors.FileDescriptor> getFileDescriptors() {
        return new HashSet<>(fileDescriptors.values());
    }

    public Descriptors.Descriptor getMessageDescriptor(String name) {
        return messageDescriptors.get(name);
    }

    public JsonFormat.Printer getJsonPrinter() {
        return jsonPrinter;
    }

    public JsonFormat.Parser getJsonParser() {
        return jsonParser;
    }
}
