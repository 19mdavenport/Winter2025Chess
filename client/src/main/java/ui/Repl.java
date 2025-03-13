package ui;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;
import java.util.Stack;

public class Repl {
    private final PrintStream out = System.out;
    private final Scanner in = new Scanner(System.in);

    private final Stack<UserInterfaceState> stateStack = new Stack<>();

    public void run(UserInterfaceState initialState) {
        out.println("Welcome to Chess!");

        stateStack.push(initialState);
        UserInterfaceState current = initialState;
        Collection<UserInterfaceOption> currentOptions = current.getOptions();
        String currentPrompt = current.getPromptText();

        while (!stateStack.isEmpty()) {
            if(stateStack.peek() != current) {
                current = stateStack.peek();
                currentOptions = current.getOptions();
                currentPrompt = current.getPromptText();
                printOptions(currentOptions);
            }
            out.print(currentPrompt);
            out.print(" >> ");
            String input = in.nextLine();

            String[] tokens = input.split(" *");
            String command = tokens[0];
            String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);

            boolean found = false;
            for(UserInterfaceOption option : currentOptions) {
                if(option.invokeOptions().contains(command)) {
                    UserInterfaceCommandOutput commandOutput;
                    try {
                        commandOutput = option.callback().apply(args);
                    } catch (Throwable t) {
                        commandOutput = new UserInterfaceCommandOutput(false, t.getMessage());
                    }
                    handleCommandOutput(commandOutput);
                    found = true;
                    break;
                }
            }

            if(!found) {
                if (!command.equals("help")) {
                    out.println(EscapeSequences.SET_TEXT_COLOR_RED +
                            "Could not find a command matching '" + command + "'." +
                            EscapeSequences.RESET_TEXT_COLOR);
                }
                printOptions(currentOptions);
            }

            out.println();
        }

        out.println("Bye! Thanks for playing!");
    }

    private void printOptions(Collection<UserInterfaceOption> options) {
        StringBuilder builder = new StringBuilder();
        for(UserInterfaceOption option : options) {
            option.invokeOptions().stream().sorted().forEach(
                    (invokeOption) -> builder.append('\'').append(invokeOption).append("', "));
            builder.delete(builder.length() - 2, builder.length());
            builder.append(" - ").append(option.description()).append("\n");
        }
        out.print(builder);
    }

    private void handleCommandOutput(UserInterfaceCommandOutput commandOutput) {
        out.print(EscapeSequences.RESET_BG_COLOR);
        out.print(commandOutput.success() ? EscapeSequences.RESET_TEXT_COLOR : EscapeSequences.SET_TEXT_COLOR_RED);
        out.print(commandOutput.output());
        out.println(EscapeSequences.RESET_TEXT_COLOR);

        if(commandOutput.popState()) {
            stateStack.pop();
        }
        if(commandOutput.newState() != null) {
            stateStack.push(commandOutput.newState());
        }
    }
}
