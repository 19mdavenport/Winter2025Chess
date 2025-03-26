package ui;

import java.util.Collection;
import java.util.stream.Collectors;

public abstract class SingleUseUIState extends UserInterfaceState {
    protected SingleUseUIState(String promptText) {
        super(promptText, true);
    }

    @Override
    protected Collection<UserInterfaceOption> createOptions() {
        return createSingleUseOptions().stream().map(SingleUseUIState::modifyOption).collect(Collectors.toList());
    }

    protected abstract Collection<UserInterfaceOption> createSingleUseOptions();

    private static UserInterfaceOption modifyOption(UserInterfaceOption option) {
        return new UserInterfaceOption(option.invokeOptions(), option.description(), option.arguments(), modifyCallback(option.callback()));
    }

    private static CommandCallback modifyCallback(CommandCallback callback) {
        return args -> modifyOutput(callback.execute(args));
    }

    private static UserInterfaceCommandOutput modifyOutput(UserInterfaceCommandOutput output) {
        return new UserInterfaceCommandOutput(output.success(), output.output(), null, true);
    }
}
