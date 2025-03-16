package com.github.officialdonut.skprotobuf.elements;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import com.github.officialdonut.skprotobuf.SkProtobuf;
import com.google.protobuf.Descriptors;
import org.jetbrains.annotations.Nullable;

import java.io.StreamCorruptedException;

public class TypeProtobufEnumValue {

    static {
        Classes.registerClass(new ClassInfo<>(Descriptors.EnumValueDescriptor.class, "protobufenumvalue")
                .name("Protobuf Enum Value")
                .user("protobuf ?enum? values?")
                .parser(new ProtbufEnumValueParser())
                .serializer(new ProtobufEnumValueSerializer()));
    }

    public static class ProtbufEnumValueParser extends Parser<Descriptors.EnumValueDescriptor> {

        @Override
        public String toString(Descriptors.EnumValueDescriptor enumValueDescriptor, int i) {
            return enumValueDescriptor.getName();
        }

        @Override
        public String toVariableNameString(Descriptors.EnumValueDescriptor enumValueDescriptor) {
            return enumValueDescriptor.getFullName();
        }

        @Override
        public @Nullable Descriptors.EnumValueDescriptor parse(String s, ParseContext context) {
            return SkProtobuf.getInstance().getProtoManager().getEnumValueDescriptor(s);
        }

        @Override
        public boolean canParse(ParseContext context) {
            return true;
        }
    }

    public static class ProtobufEnumValueSerializer extends Serializer<Descriptors.EnumValueDescriptor> {

        @Override
        public Fields serialize(Descriptors.EnumValueDescriptor enumValueDescriptor) {
            Fields fields = new Fields();
            fields.putObject("name", enumValueDescriptor.getFullName());
            return fields;
        }

        @Override
        protected Descriptors.EnumValueDescriptor deserialize(Fields fields) throws StreamCorruptedException {
            return SkProtobuf.getInstance().getProtoManager().getEnumValueDescriptor(fields.getObject("name", String.class));
        }

        @Override
        public void deserialize(Descriptors.EnumValueDescriptor enumValueDescriptor, Fields fields) {}

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
