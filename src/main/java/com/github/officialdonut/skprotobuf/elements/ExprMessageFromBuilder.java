package com.github.officialdonut.skprotobuf.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.protobuf.Message;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Protobuf Message From Builder")
public class ExprMessageFromBuilder extends SimpleExpression<Message> {

    static {
        Skript.registerExpression(ExprMessageFromBuilder.class, Message.class, ExpressionType.COMBINED, "proto[buf] [message] from [builder] %protobufmessagebuilder%");
    }

    private Expression<Message.Builder> exprBuilder;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        exprBuilder = (Expression<Message.Builder>) expressions[0];
        return true;
    }

    @Override
    protected @Nullable Message[] get(Event event) {
        Message.Builder builder = exprBuilder.getSingle(event);
        return builder != null ? new Message[]{builder.build()} : null;
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
        return "protobuf message from " + exprBuilder.toString(event, b);
    }
}
