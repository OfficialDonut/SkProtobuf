package com.github.officialdonut.skprotobuf.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.protobuf.Descriptors;
import com.google.protobuf.MessageOrBuilder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Name("Protobuf Message Field Value")
public class ExprFieldValue extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprFieldValue.class, Object.class, ExpressionType.COMBINED,
                "value[:s] of proto[buf] [message] field %string% in %protobufmessageorbuilder%",
                "proto[buf] [message] field %string%'s value[:s] in %protobufmessageorbuilder%");
    }

    private Expression<String> exprField;
    private Expression<MessageOrBuilder> exprMessage;
    private boolean repeatedField;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        exprField = (Expression<String>) expressions[0];
        exprMessage = (Expression<MessageOrBuilder>) expressions[1];
        repeatedField = parseResult.hasTag("s");
        return true;
    }

    @Override
    protected @Nullable Object[] get(Event event) {
        String field = exprField.getSingle(event);
        MessageOrBuilder message = exprMessage.getSingle(event);
        if (field != null && message != null) {
            Descriptors.FieldDescriptor fieldDescriptor = message.getDescriptorForType().findFieldByName(field);
            if (fieldDescriptor != null) {
                Object value = message.getField(fieldDescriptor);
                if (repeatedField) {
                    return value instanceof List<?> list ? list.toArray(Object[]::new) : new Object[0];
                } else {
                    return new Object[]{value};
                }
            }
        }
        return repeatedField ? new Object[0] : null;
    }

    @Override
    public boolean isSingle() {
        return !repeatedField;
    }

    @Override
    public Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "value of protobuf message field " + exprField.toString(event, b) + " in " + exprMessage.toString(event, b);
    }
}
