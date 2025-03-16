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

@Name("Protobuf Message Builder")
public class ExprMessageBuilder extends SimpleExpression<Message.Builder> {

    static {
        Skript.registerExpression(ExprMessageBuilder.class, Message.Builder.class, ExpressionType.COMBINED,
                "[new] builder for proto[buf] [message] %*string%",
                "%protobufmessage% as proto[buf] [message] builder",
                "[:new] builder for proto[buf] field %string% (in|of) %protobufmessagebuilder%");
    }

    // syntax 1
    private Descriptors.Descriptor descriptor;

    // syntax 2
    private Expression<Message> exprMessage;

    // syntax 3
    private boolean newBuilder;
    private Expression<String> exprField;
    private Expression<Message.Builder> exprBuilder;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        if (matchedPattern == 0) {
            String messageName = ((Literal<String>) expressions[0]).getSingle();
            descriptor = SkProtobuf.getInstance().getProtoManager().getMessageDescriptor(messageName);
            if (descriptor == null) {
                Skript.error("Failed to find descriptor for message: " + messageName);
                return false;
            }
        } else if (matchedPattern == 1) {
            exprMessage = (Expression<Message>) expressions[0];
        } else {
            newBuilder = parseResult.hasTag("new");
            exprField = (Expression<String>) expressions[0];
            exprBuilder = (Expression<Message.Builder>) expressions[1];
        }
        return true;
    }

    @Override
    protected @Nullable Message.Builder[] get(Event event) {
        if (descriptor != null) {
            return new Message.Builder[]{DynamicMessage.newBuilder(descriptor)};
        } else if (exprMessage != null) {
            Message message = exprMessage.getSingle(event);
            return message != null ? new Message.Builder[]{message.toBuilder()} : null;
        } else {
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
        return "protobuf message builder";
    }
}
