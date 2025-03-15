package client;

import exception.ResponseException;
import model.UserData;
import ui.CommandArgument;
import ui.UserInterfaceCommandOutput;
import ui.UserInterfaceOption;
import ui.UserInterfaceState;
import web.ServerFacade;

import java.util.Collection;
import java.util.List;

public class PreLoginUIState extends UserInterfaceState {
    private final ServerFacade server;

    public PreLoginUIState(ServerFacade server) {
        super("Chess Login", false);
        this.server = server;
    }

    @Override
    public Collection<UserInterfaceOption> createOptions() {
        return List.of(
                new UserInterfaceOption(List.of("r", "register"), "create an account",
                        List.of(new CommandArgument("Username", String.class),
                                new CommandArgument("Password", String.class),
                                new CommandArgument("Email", String.class)),
                        this::register),
                new UserInterfaceOption(List.of("l", "login"), "sign in as an existing account",
                        List.of(new CommandArgument("Username", String.class),
                                new CommandArgument("Password", String.class)),
                        this::login),
                new UserInterfaceOption(List.of("q", "quit"), "quit program", List.of(), this::quit)
        );
    }

    private UserInterfaceCommandOutput register(Object[] params) throws ResponseException {
        server.register(new UserData((String) params[0], (String) params[1], (String) params[2]));
        return UserInterfaceCommandOutput.newState("User registered successfully. Welcome " + params[0] + "!",
                new PostLoginUIState(server, (String) params[0]));
    }

    private UserInterfaceCommandOutput login(Object[] params) throws ResponseException {
        server.login(new UserData((String) params[0], (String) params[1], null));
        return UserInterfaceCommandOutput.newState("Signed in successfully. Welcome back " + params[0] + "!",
                new PostLoginUIState(server, (String) params[0]));
    }

    private UserInterfaceCommandOutput quit(Object[] params) {
        return UserInterfaceCommandOutput.popState("Quitting . . .");
    }
}
