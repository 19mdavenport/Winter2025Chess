package ui;

import java.util.List;

public record UserInterfaceOption(
        List<String> invokeOptions,
        String description,
        List<CommandArgument> arguments,
        CommandCallback callback) {
}
