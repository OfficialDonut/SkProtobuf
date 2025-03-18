package com.github.officialdonut.skprotobuf.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.github.officialdonut.skprotobuf.ProtoManager;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Modify Protobuf Message Builder Field")
public class EffModifyField extends Effect {

    static {
        Skript.registerEffect(EffModifyField.class,
                "set proto[buf] [message] field %string% (in|of) %protobufmessagebuilder% to %objects%",
                "add %objects% to proto[buf] [message] field %string% (in|of) %protobufmessagebuilder%",
                "clear proto[buf] [message] field %string% (in|of) %protobufmessagebuilder%");
    }

    private int matchedPattern;
    private Expression<String> exprField;
    private Expression<Message.Builder> exprBuilder;
    private Expression<Object> exprDelta;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        this.matchedPattern = matchedPattern;
        if (matchedPattern == 0) {
            exprField = (Expression<String>) expressions[0];
            exprBuilder = (Expression<Message.Builder>) expressions[1];
            exprDelta = LiteralUtils.defendExpression(expressions[2]);
            return LiteralUtils.canInitSafely(exprDelta);
        } else if (matchedPattern == 1) {
            exprField = (Expression<String>) expressions[1];
            exprBuilder = (Expression<Message.Builder>) expressions[2];
            exprDelta = LiteralUtils.defendExpression(expressions[0]);
            return LiteralUtils.canInitSafely(exprDelta);
        } else {
            exprField = (Expression<String>) expressions[0];
            exprBuilder = (Expression<Message.Builder>) expressions[1];
            return true;
        }
    }

    @Override
    protected void execute(Event event) {
        String field = exprField.getSingle(event);
        Message.Builder builder = exprBuilder.getSingle(event);
        if (field == null || builder == null) {
            return;
        }
        Descriptors.FieldDescriptor fieldDescriptor = builder.getDescriptorForType().findFieldByName(field);
        if (fieldDescriptor == null) {
            return;
        }
        if (matchedPattern == 0) {
            if (fieldDescriptor.isRepeated()) {
                builder.clearField(fieldDescriptor);
                for (Object delta : exprDelta.getArray(event)) {
                    builder.addRepeatedField(fieldDescriptor, ProtoManager.convertObject(delta, fieldDescriptor.getJavaType()));
                }
            } else {
                Object delta = exprDelta.getSingle(event);
                if (delta != null) {
                    builder.setField(fieldDescriptor, ProtoManager.convertObject(delta, fieldDescriptor.getJavaType()));
                } else {
                    builder.clearField(fieldDescriptor);
                }
            }
        } else if (matchedPattern == 1) {
            for (Object object : exprDelta.getArray(event)) {
                builder.addRepeatedField(fieldDescriptor, ProtoManager.convertObject(object, fieldDescriptor.getJavaType()));
            }
        } else {
            builder.clearField(fieldDescriptor);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "modify protobuf message builder field";
    }
}
