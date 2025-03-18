package com.github.officialdonut.skprotobuf.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Name;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.*;
import ch.njol.util.Kleenean;
import com.github.officialdonut.skprotobuf.ProtoManager;
import com.github.officialdonut.skprotobuf.SkProtobuf;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Name("Protobuf Message Builder")
public class ExprMessageBuilder extends SectionExpression<Message.Builder> {

    static {
        Skript.registerExpression(ExprMessageBuilder.class, Message.Builder.class, ExpressionType.COMBINED, "[new] builder for proto[buf] [message] %*string%");
    }

    private Descriptors.Descriptor descriptor;
    private Map<Descriptors.FieldDescriptor, Expression<Object>> fields;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult, @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> list) {
        String messageName = ((Literal<String>) expressions[0]).getSingle();
        descriptor = SkProtobuf.getInstance().getProtoManager().getMessageDescriptor(messageName);
        if (descriptor == null) {
            Skript.error("Failed to find descriptor for message: " + messageName);
            return false;
        }

        fields = new HashMap<>();
        if (sectionNode != null) {
            EntryValidator.EntryValidatorBuilder entryValidator = EntryValidator.builder();
            for (Descriptors.FieldDescriptor field : descriptor.getFields()) {
                entryValidator.addEntryData(new ExpressionEntryData<>(field.getName(), null, true, ProtoManager.getConvertibleSupertype(field.getJavaType())));
            }
            EntryContainer entryContainer = entryValidator.build().validate(sectionNode);
            if (entryContainer == null) {
                return false;
            }
            for (Descriptors.FieldDescriptor field : descriptor.getFields()) {
                if (entryContainer.hasEntry(field.getName())) {
                    fields.put(field, entryContainer.get(field.getName(), Object.class, false));
                }
            }
        }

        return true;
    }

    @Override
    protected @Nullable Message.Builder[] get(Event event) {
        Message.Builder builder = DynamicMessage.newBuilder(descriptor);
        for (Map.Entry<Descriptors.FieldDescriptor, Expression<Object>> entry : fields.entrySet()) {
            Descriptors.FieldDescriptor field = entry.getKey();
            if (field.isRepeated()) {
                for (Object object : entry.getValue().getArray(event)) {
                    builder.addRepeatedField(field, ProtoManager.convertObject(object, field.getJavaType()));
                }
            } else {
                Object fieldValue = entry.getValue().getSingle(event);
                if (fieldValue != null) {
                    builder.setField(field, ProtoManager.convertObject(fieldValue, field.getJavaType()));
                }
            }
        }
        return new Message.Builder[]{builder};
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
        return "builder for protobuf message " + descriptor;
    }
}
