package ui;

import java.util.Collection;

public interface UserInterfaceState {
    Collection<UserInterfaceOption> getOptions();
    String getPromptText();
}
