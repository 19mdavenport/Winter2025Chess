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

public class PreLoginUIState implements UserInterfaceState {
    private final ServerFacade server;

    public PreLoginUIState(ServerFacade server) {
        this.server = server;
    }


    @Override
    public Collection<UserInterfaceOption> getOptions() {
        return List.of(
                new UserInterfaceOption(List.of("r", "register"), "create an account",
                        List.of(new CommandArgument("username", String.class),
                                new CommandArgument("password", String.class),
                                new CommandArgument("email", String.class)),
                        this::register),
                new UserInterfaceOption(List.of("l", "login"), "sign in as an existing account",
                        List.of(new CommandArgument("username", String.class),
                                new CommandArgument("password", String.class)),
                        this::login),
                new UserInterfaceOption(List.of("q", "quit"), "quit program", List.of(), this::quit)
        );
    }

    @Override
    public String getPromptText() {
        return "Chess Login";
    }

    private UserInterfaceCommandOutput register(Object[] params) throws ResponseException {
        server.register(new UserData((String) params[0], (String) params[1], (String) params[2]));
        return new UserInterfaceCommandOutput("User registered successfully. Welcome " + params[0] + "!",
                null); //FIXME
    }

    private UserInterfaceCommandOutput login(Object[] params) throws ResponseException {
        server.login(new UserData((String) params[0], (String) params[1], null));
        return new UserInterfaceCommandOutput("Signed in successfully. Welcome back " + params[0] + "!",
                null); //FIXME
    }

    private UserInterfaceCommandOutput quit(Object[] params) {
        return UserInterfaceCommandOutput.popState("Quitting . . .");
    }
}
