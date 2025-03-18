package com.github.officialdonut.skprotobuf.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Builder For Protobuf Message Field")
public class ExprBuilderForField extends SimpleExpression<Message.Builder> {

    static {
        Skript.registerExpression(ExprBuilderForField.class, Message.Builder.class, ExpressionType.COMBINED, "[:new] builder for proto[buf] field %string% (in|of) %protobufmessagebuilder%");
    }

    private boolean newBuilder;
    private Expression<String> exprField;
    private Expression<Message.Builder> exprBuilder;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        newBuilder = parseResult.hasTag("new");
        exprField = (Expression<String>) expressions[0];
        exprBuilder = (Expression<Message.Builder>) expressions[1];
        return true;
    }

    @Override
    protected @Nullable Message.Builder[] get(Event event) {
        String field = exprField.getSingle(event);
        Message.Builder builder = exprBuilder.getSingle(event);
        if (field != null && builder != null) {
            Descriptors.FieldDescriptor fieldDescriptor = builder.getDescriptorForType().findFieldByName(field);
            if (fieldDescriptor != null) {
                return new Message.Builder[]{newBuilder ? builder.newBuilderForField(fieldDescriptor) : builder.getFieldBuilder(fieldDescriptor)};
            }
        }
        return null;
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
        return "builder for protobuf field " + exprField.toString(event, b) + " in " + exprBuilder.toString(event, b);
    }
}
