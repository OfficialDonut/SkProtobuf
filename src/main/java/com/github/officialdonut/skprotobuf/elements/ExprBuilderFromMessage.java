package com.github.officialdonut.skprotobuf.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.protobuf.Message;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Builder From Protobuf Message")
public class ExprBuilderFromMessage extends SimpleExpression<Message.Builder> {

    static {
        Skript.registerExpression(ExprBuilderFromMessage.class, Message.Builder.class, ExpressionType.COMBINED,
                "builder from proto[buf] [message] %protobufmessage%",
                "%protobufmessage% as proto[buf] [message] builder");
    }

    private Expression<Message> exprMessage;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        exprMessage = (Expression<Message>) expressions[0];
        return true;
    }

    @Override
    protected @Nullable Message.Builder[] get(Event event) {
        Message message = exprMessage.getSingle(event);
        return message != null ? new Message.Builder[]{message.toBuilder()} : null;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Message.Builder> getReturnType() {
        return Message.Builder.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "builder from protobuf message " + exprMessage.toString(event, b);
    }
}
