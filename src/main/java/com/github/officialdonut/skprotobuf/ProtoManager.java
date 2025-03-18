package com.github.officialdonut.skprotobuf;

import com.google.protobuf.*;
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
    private Map<String, Descriptors.EnumValueDescriptor> enumValueDescriptors;
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
        enumValueDescriptors = new HashMap<>();

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
                for (Descriptors.EnumDescriptor enumDescriptor : fileDescriptor.getEnumTypes()) {
                    for (Descriptors.EnumValueDescriptor descriptor : enumDescriptor.getValues()) {
                        enumValueDescriptors.put(descriptor.getFullName(), descriptor);
                        enumValueDescriptors.putIfAbsent(descriptor.getName(), descriptor);
                    }
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

    public static Object convertObject(Object object, Descriptors.FieldDescriptor.JavaType type) {
        return switch (type) {
            case INT -> object instanceof Number n ? n.intValue() : object;
            case LONG -> object instanceof Number n ? n.longValue() : object;
            case FLOAT -> object instanceof Number n ? n.floatValue() : object;
            case DOUBLE -> object instanceof Number n ? n.doubleValue() : object;
            case MESSAGE -> object instanceof Message.Builder builder ? builder.build() : object;
            default -> object;
        };
    }

    public static Class<?> getConvertibleSupertype(Descriptors.FieldDescriptor.JavaType type) {
        return switch (type) {
            case INT, LONG, FLOAT, DOUBLE -> Number.class;
            case BOOLEAN -> Boolean.class;
            case STRING -> String.class;
            case ENUM -> Descriptors.EnumValueDescriptor.class;
            case MESSAGE -> MessageOrBuilder.class;
            default -> Object.class;
        };
    }

    public Set<Descriptors.FileDescriptor> getFileDescriptors() {
        return new HashSet<>(fileDescriptors.values());
    }

    public Descriptors.Descriptor getMessageDescriptor(String name) {
        return messageDescriptors.get(name);
    }

    public Descriptors.EnumValueDescriptor getEnumValueDescriptor(String name) {
        return enumValueDescriptors.get(name);
    }

    public JsonFormat.Printer getJsonPrinter() {
        return jsonPrinter;
    }

    public JsonFormat.Parser getJsonParser() {
        return jsonParser;
    }
}
