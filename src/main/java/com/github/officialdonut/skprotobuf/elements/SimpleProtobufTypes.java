package com.github.officialdonut.skprotobuf.elements;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;

public class SimpleProtobufTypes {

    static {
        Classes.registerClass(new ClassInfo<>(MessageOrBuilder.class, "protobufmessageorbuilder")
                .name("Protobuf Message Or Builder")
                .user("protobuf ?messages? or ?builders?"));

        Classes.registerClass(new ClassInfo<>(Message.Builder.class, "protobufmessagebuilder")
                .name("Protobuf Message Builder")
                .user("protobuf ?message? builders?"));

        Classes.registerClass(new ClassInfo<>(Any.class, "protobufany")
                .name("Protobuf Any")
                .user("protobuf ?any"));
    }
}
