package ui;

import java.util.function.Predicate;

public record CommandArgument<T>(String argName, Class<T> argType, Predicate<Object[]> isRequired, T defaultValue) {
    public CommandArgument(String argName, Class<T> argType) {
        this(argName, argType, (objs) -> true, null);
    }
}
