package com.github.officialdonut.skprotobuf.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.protobuf.Descriptors;
import com.google.protobuf.MessageOrBuilder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Protobuf Message Field Is Set")
public class CondIsFieldSet extends Condition {

    static {
        Skript.registerCondition(CondIsFieldSet.class,
                "proto[buf] [message] field[s] %strings% (is|are) set in %protobufmessageorbuilders%",
                "proto[buf] [message] field[s] %strings% (isn't|is not|aren't|are not) set in %protobufmessageorbuilders%");
    }

    private Expression<String> exprField;
    private Expression<MessageOrBuilder> exprMessage;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        exprField = (Expression<String>) expressions[0];
        exprMessage = (Expression<MessageOrBuilder>) expressions[1];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(Event event) {
        return exprMessage.check(event, message -> exprField.check(event, field -> {
            Descriptors.FieldDescriptor fieldDescriptor = message.getDescriptorForType().findFieldByName(field);
            return fieldDescriptor != null && message.hasField(fieldDescriptor);
        }), isNegated());
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return PropertyCondition.toString(this, PropertyCondition.PropertyType.BE, event, b, exprField, "set in " + exprMessage.toString(event, b));
    }
}
