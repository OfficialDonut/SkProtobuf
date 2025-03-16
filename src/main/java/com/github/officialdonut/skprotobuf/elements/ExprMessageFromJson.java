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
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

@Name("Protobuf Message From Json")
public class ExprMessageFromJson extends SimpleExpression<Message> {

    static {
        Skript.registerExpression(ExprMessageFromJson.class, Message.class, ExpressionType.COMBINED,
                "%string% [parsed] as proto[buf] [message] %*string%",
                "proto[buf] [message] %*string% from json %string%");
    }

    private Expression<String> exprJson;
    private Descriptors.Descriptor descriptor;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        exprJson = (Expression<String>) expressions[matchedPattern == 0 ? 0 : 1];
        String messageName = ((Literal<String>) expressions[matchedPattern == 0 ? 1 : 0]).getSingle();
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
            String json = exprJson.getSingle(event);
            if (json != null) {
                Message.Builder builder = DynamicMessage.newBuilder(descriptor);
                SkProtobuf.getInstance().getProtoManager().getJsonParser().merge(json, builder);
                return new Message[]{builder.build()};
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
        return exprJson.toString(event, b) + " parsed as protobuf message " + descriptor;
    }
}
