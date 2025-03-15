package ui;

import java.util.Collection;

public abstract class UserInterfaceState {
    private final String promptText;
    private final boolean singleUse;
    private Collection<UserInterfaceOption> options;

    protected UserInterfaceState(String promptText, boolean singleUse) {
        this.promptText = promptText;
        this.singleUse = singleUse;
    }

    protected abstract Collection<UserInterfaceOption> createOptions();

    public Collection<UserInterfaceOption> getOptions() {
        if (options == null) {
            options = createOptions();
        }
        return options;
    }

    public String getPromptText() {
        return promptText;
    }

    public boolean isSingleUse() {
        return singleUse;
    }
}
