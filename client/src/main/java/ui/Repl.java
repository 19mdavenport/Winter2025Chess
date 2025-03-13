package ui;

import java.io.PrintStream;
import java.util.*;
import java.util.function.Consumer;

public class Repl {
    private static final UserInterfaceOption HELP_OPTION =
            new UserInterfaceOption(List.of("h", "help"), "display command options", List.of(), null);

    private final PrintStream out = System.out;
    private final Scanner in = new Scanner(System.in);

    private final Stack<UserInterfaceState> stateStack = new Stack<>();

    public void run(UserInterfaceState initialState) {
        out.println("Welcome to Chess!");

        stateStack.push(initialState);
        UserInterfaceState current = initialState;
        Collection<UserInterfaceOption> currentOptions = current.getOptions();
        String currentPrompt = current.getPromptText();

        printOptions(currentOptions);
        out.println();

        while (!stateStack.isEmpty()) {
            if (stateStack.peek() != current) {
                current = stateStack.peek();
                currentOptions = current.getOptions();
                currentPrompt = current.getPromptText();
                printOptions(currentOptions);
            }
            out.print(currentPrompt);
            out.print(" >> ");

            String command = in.nextLine();
            executeCommand(command, currentOptions);

            out.println();
        }

        out.println("Bye! Thanks for playing!");
    }

    private void printOptions(Collection<UserInterfaceOption> options) {
        StringBuilder builder = new StringBuilder("Command Options:\n");
        options = new ArrayList<>(options);
        options.add(HELP_OPTION);
        for (UserInterfaceOption option : options) {
            option.invokeOptions().stream()
                    .sorted(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()))
                    .forEach((invokeOption) -> builder.append('\'').append(invokeOption).append("', "));
            builder.delete(builder.length() - 2, builder.length());
            builder.append(" - ").append(option.description()).append("\n");
        }
        out.print(builder);
    }

    private void executeCommand(String command, Collection<UserInterfaceOption> currentOptions) {
        boolean found = false;
        for (UserInterfaceOption option : currentOptions) {
            if (option.invokeOptions().contains(command)) {
                Object[] args = readArgs(option.arguments());
                if (args == null) {
                    break;
                }

                UserInterfaceCommandOutput commandOutput;
                try {
                    commandOutput = option.callback().execute(args);
                } catch (Throwable t) {
                    commandOutput = new UserInterfaceCommandOutput(false, t.getMessage());
                }
                handleCommandOutput(commandOutput);

                found = true;
                break;
            }
        }

        if (!found) {
            if (!HELP_OPTION.invokeOptions().contains(command)) {
                out.println(EscapeSequences.SET_TEXT_COLOR_RED +
                        "Could not find a command matching '" + command + "'." +
                        EscapeSequences.RESET_TEXT_COLOR);
            }
            printOptions(currentOptions);
        }
    }

    private Object[] readArgs(List<CommandArgument> arguments) {
        Object[] ret = new Object[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            CommandArgument argument = arguments.get(i);
            Object argValue = null;

            while (argValue == null) {
                out.print("\t" + argument.argName() + ": ");

                String value = in.nextLine();
                if (value.isBlank()) {
                    return null;
                }

                try {
                    argValue = getArgValue(value, argument.argType());
                } catch (IllegalArgumentException e) {
                    printAsError(value + " not a valid value for " + argument.argName() + " argument. Enter empty value to quit");
                }
            }

            ret[i] = argValue;
        }
        return ret;
    }

    private <T> T getArgValue(String value, Class<T> type) throws IllegalArgumentException {
        if (type.isAssignableFrom(String.class)) {
            return type.cast(value);
        }
        if (type.isAssignableFrom(Integer.class)) {
            try {
                return type.cast(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(value, e);
            }
        }
        if (type.isEnum()) {
            return (T) Enum.valueOf((Class<? extends Enum>) type, value);
        }
        throw new IllegalStateException(value + " is not a valid argument type");
    }

    private void handleCommandOutput(UserInterfaceCommandOutput commandOutput) {
        Consumer<String> printFunc = commandOutput.success() ? this::printAsSuccess : this::printAsError;
        printFunc.accept(commandOutput.output());

        if (commandOutput.popState()) {
            stateStack.pop();
        }
        if (commandOutput.newState() != null) {
            stateStack.push(commandOutput.newState());
        }
    }

    private void printAsSuccess(String str) {
        out.print(EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR + str);
    }

    private void printAsError(String str) {
        out.print(EscapeSequences.RESET_BG_COLOR + EscapeSequences.SET_TEXT_COLOR_RED +
                str + EscapeSequences.RESET_TEXT_COLOR);
    }
}
