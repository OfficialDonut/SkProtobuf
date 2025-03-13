package com.github.officialdonut.skprotobuf.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import com.github.officialdonut.skprotobuf.SkProtobuf;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.logging.Level;

public class TypeProtobufMessage {

    static {
        Classes.registerClass(new ClassInfo<>(Message.class, "protobufmessage")
                .name("Protobuf Message")
                .parser(new ProtbufMessageParser())
                .serializer(new ProtobufMessageSerializer()));
    }

    public static class ProtbufMessageParser extends Parser<Message> {

        @Override
        public String toString(Message message, int i) {
            try {
                return SkProtobuf.getInstance().getProtoManager().getJsonPrinter().print(message);
            } catch (InvalidProtocolBufferException e) {
                SkProtobuf.getInstance().getLogger().log(Level.WARNING, "Failed to print protobuf message", e);
                return null;
            }
        }

        @Override
        public String toVariableNameString(Message message) {
            return toString(message, 0);
        }

        @Override
        public boolean canParse(ParseContext context) {
            return false;
        }
    }

    public static class ProtobufMessageSerializer extends Serializer<Message> {

        @Override
        public Fields serialize(Message message) {
            Fields fields = new Fields();
            fields.putObject("name", message.getDescriptorForType().getFullName());
            fields.putObject("bytes", message.toByteArray());
            return fields;
        }

        @Override
        protected Message deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
            Descriptors.Descriptor descriptor = SkProtobuf.getInstance().getProtoManager().getMessageDescriptor(fields.getObject("name", String.class));
            if (descriptor != null) {
                try {
                    return DynamicMessage.parseFrom(descriptor, fields.getObject("bytes", byte[].class));
                } catch (InvalidProtocolBufferException e) {
                    SkProtobuf.getInstance().getLogger().log(Level.WARNING, "Failed to parse protobuf message", e);
                }
            }
            throw new NotSerializableException();
        }

        @Override
        public void deserialize(Message message, Fields fields) {}

        @Override
        public boolean mustSyncDeserialization() {
            return false;
        }

        @Override
        protected boolean canBeInstantiated() {
            return false;
        }
    }
}
