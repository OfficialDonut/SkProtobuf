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
import com.google.protobuf.*;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

@Name("Unpack Protobuf Any")
public class ExprUnpackAny extends SimpleExpression<Message> {

    static {
        Skript.registerExpression(ExprUnpackAny.class, Message.class, ExpressionType.COMBINED, "%protobufany% unpacked as proto[buf] [message] %*string%");
    }

    private Expression<Any> exprAny;
    private Descriptors.Descriptor descriptor;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        exprAny = (Expression<Any>) expressions[0];
        String messageName = ((Literal<String>) expressions[1]).getSingle();
        descriptor = SkProtobuf.getInstance().getProtoManager().getMessageDescriptor(messageName);
        if (descriptor == null) {
            Skript.error("Failed to find descriptor for message: " + messageName);
            return false;
        }
        return true;
    }

    @Override
    protected @Nullable Message[] get(Event event) {
        try {
            Any any = exprAny.getSingle(event);
            if (any != null) {
                return new Message[]{DynamicMessage.parseFrom(descriptor, any.getValue())};
            }
        } catch (InvalidProtocolBufferException e) {
            SkProtobuf.getInstance().getLogger().log(Level.WARNING,"Failed to parse protobuf message", e);
        }
        return null;
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
        return descriptor + " unpacked as protobuf message " + exprAny.toString(event, b);
    }
}
