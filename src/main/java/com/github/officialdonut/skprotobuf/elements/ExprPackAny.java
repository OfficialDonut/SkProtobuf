package com.github.officialdonut.skprotobuf.elements;


import ch.njol.skript.Skript;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Pack Protobuf Any")
public class ExprPackAny extends SimpleExpression<Any> {

    static {
        Skript.registerExpression(ExprPackAny.class, Any.class, ExpressionType.COMBINED, "%protobufmessage% packed as proto[buf] any");
    }

    private Expression<Message> exprMessage;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        exprMessage = (Expression<Message>) expressions[0];
        return true;
    }

    @Override
    protected @Nullable Any[] get(Event event) {
        Message message = exprMessage.getSingle(event);
        return message != null ? new Any[]{Any.pack(message)} : null;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Any> getReturnType() {
        return Any.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return exprMessage.toString(event, b) + " packed as protobuf any";
    }
}
