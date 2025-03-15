package ui;

public record UserInterfaceCommandOutput(boolean success, String output, UserInterfaceState newState, boolean popState) {

    public static UserInterfaceCommandOutput success(String output) {
        return new UserInterfaceCommandOutput(true, output, null, false);
    }

    public static UserInterfaceCommandOutput failure(String output) {
        return new UserInterfaceCommandOutput(false, output, null, false);
    }

    public static UserInterfaceCommandOutput newState(String output, UserInterfaceState newState) {
        return new UserInterfaceCommandOutput(true, output, newState, false);
    }

    public static UserInterfaceCommandOutput popState(String output) {
        return new UserInterfaceCommandOutput(true, output, null, true);
    }
}
