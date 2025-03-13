package ui;

public record UserInterfaceCommandOutput(boolean success, String output, UserInterfaceState newState, boolean popState) {

    public UserInterfaceCommandOutput(String output) {
        this(true, output);
    }

    public UserInterfaceCommandOutput(boolean success, String output) {
        this(success, output, null, false);
    }

    public UserInterfaceCommandOutput(String output, UserInterfaceState newState) {
        this(true, output, newState, false);
    }

    public static UserInterfaceCommandOutput popState(String output) {
        return new UserInterfaceCommandOutput(true, output, null, true);
    }
}
