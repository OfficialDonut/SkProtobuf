package com.github.officialdonut.skprotobuf.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.github.officialdonut.skprotobuf.SkProtobuf;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Protobuf Message Default Instance")
public class ExprDefaultInstance extends SimpleExpression<Message> {

    static {
        Skript.registerExpression(ExprDefaultInstance.class, Message.class, ExpressionType.COMBINED, "default instance of proto[buf] [message] %*string%");
    }

    private Descriptors.Descriptor descriptor;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        String messageName = ((Literal<String>) expressions[0]).getSingle();
        descriptor = SkProtobuf.getInstance().getProtoManager().getMessageDescriptor(messageName);
        if (descriptor == null) {
            Skript.error("Failed to find descriptor for message: " + messageName);
            return false;
        }
        return true;
    }

    @Override
    protected @Nullable Message[] get(Event event) {
        return new Message[]{DynamicMessage.getDefaultInstance(descriptor)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Message> getReturnType() {
        return Message.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "default instance of protobuf message " + descriptor;
    }
}
