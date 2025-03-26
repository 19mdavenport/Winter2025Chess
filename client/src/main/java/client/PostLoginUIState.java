package client;

import chess.ChessGame;
import exception.ResponseException;
import model.GameData;
import model.JoinGameRequest;
import model.ListGamesResponse;
import ui.*;
import web.ServerFacade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class PostLoginUIState extends UserInterfaceState {
    private final ServerFacade server;
    private final String username;

    public PostLoginUIState(ServerFacade server, String username) {
        super("Chess", true);
        this.server = server;
        this.username = username;
    }

    @Override
    public Collection<UserInterfaceOption> createOptions() {
        return List.of(
                new UserInterfaceOption(List.of("log", "out", "logout"),
                        "logout out of '" + EscapeSequences.SET_TEXT_COLOR_MAGENTA + username +
                                EscapeSequences.RESET_TEXT_COLOR + "' account",
                        List.of(), this::logout),
                new UserInterfaceOption(List.of("c", "create", "new"),
                        "create a new game of chess",
                        List.of(new CommandArgument<>("Name of game", String.class)), this::create),
                new UserInterfaceOption(List.of("l", "list", "games"), "view current games", List.of(), this::list),
                new UserInterfaceOption(List.of("p", "play", "j", "join"),
                        "join a game of chess",
                        List.of(new CommandArgument<>("Game number", Integer.class),
                                new CommandArgument<>("Color [white/black] ", ChessGame.TeamColor.class)), this::play),
                new UserInterfaceOption(List.of("o", "observe", "w", "watch"),
                        "watch a game of chess",
                        List.of(new CommandArgument<>("Game number", Integer.class)), this::observe)

        );
    }

    private UserInterfaceCommandOutput logout(Object[] params) {
        try {
            server.logout();
        } catch (ResponseException ignored) {
        }
        return UserInterfaceCommandOutput.popState("Logged out.");
    }

    private UserInterfaceCommandOutput create(Object[] params) throws ResponseException {
        int gameNum = server.createGame((String) params[0]);
        return UserInterfaceCommandOutput.success(
                "Game " + params[0] + " successfully created. It has game number " + gameNum + ".");
    }

    private UserInterfaceCommandOutput list(Object[] params) throws ResponseException {
        ListGamesResponse listResponse = server.listGames();
        if (listResponse.games().isEmpty()) {
            return UserInterfaceCommandOutput.newState("There are no active games. Would you like to make one?",
                    new NoGamesCreateGameUIState());
        }
        List<GameData> orderedGames = new ArrayList<>(listResponse.games());
        orderedGames.sort(Comparator.comparingInt(GameData::gameID));

        int longestName = 1;
        int longestWhiteUsername = 4;
        for (GameData game : orderedGames) {
            if (game.gameName().length() > longestName) {
                longestName = game.gameName().length();
            }
            if (game.whiteUsername() != null && game.whiteUsername().length() > longestWhiteUsername) {
                longestWhiteUsername = game.whiteUsername().length();
            }
        }

        int gameNumOffset = String.valueOf(orderedGames.size()).length() + 1;
        int gameNameOffset = longestName + 4;
        int whiteUsernameOffset = longestWhiteUsername + 4;

        StringBuilder out = new StringBuilder("Current games:\n");
        for (GameData game : orderedGames) {
            out.append(game.gameID()).append(". ");
            out.append(" ".repeat(gameNumOffset - String.valueOf(game.gameID()).length()));

            out.append("Game name: ").append(game.gameName());
            out.append(" ".repeat(gameNameOffset - game.gameName().length()));

            if (game.whiteUsername() == null) {
                out.append("White: ").append(EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY)
                        .append("No Player").append(EscapeSequences.RESET_TEXT_COLOR)
                        .append(" ".repeat(whiteUsernameOffset - 2));
            } else {
                out.append("White Player: ").append(game.whiteUsername())
                        .append(" ".repeat(whiteUsernameOffset - game.whiteUsername().length()));
            }

            if (game.blackUsername() == null) {
                out.append("Black: ").append(EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY)
                        .append("No Player").append(EscapeSequences.RESET_TEXT_COLOR);
            } else {
                out.append("Black Player: ").append(game.blackUsername());
            }
            out.append("\n");
        }
        out.deleteCharAt(out.length() - 1);
        return UserInterfaceCommandOutput.success(out.toString());
    }

    private UserInterfaceCommandOutput play(Object[] params) throws ResponseException {
        ChessGame.TeamColor perspective = (ChessGame.TeamColor) params[1];
        GameUIState next = new GameUIState(true, server, new BoardPrinter(perspective));
        server.joinGame(new JoinGameRequest(perspective, (Integer) params[0]), next);
        return UserInterfaceCommandOutput.newState("", next);
    }

    private UserInterfaceCommandOutput observe(Object[] params) throws ResponseException {
        GameUIState next = new GameUIState(false, server, new BoardPrinter(ChessGame.TeamColor.WHITE));
        server.observeGame((int) params[0], next);
        return UserInterfaceCommandOutput.newState("", next);
    }

    private class NoGamesCreateGameUIState extends SingleUseUIState {

        public NoGamesCreateGameUIState() {
            super(PostLoginUIState.this.getPromptText() + " > Create new game? [y/n]");
        }

        @Override
        protected Collection<UserInterfaceOption> createSingleUseOptions() {
            return List.of(
                    new UserInterfaceOption(List.of("y", "yes"), "create a new game of chess",
                            List.of(new CommandArgument<>("name of game", String.class)), PostLoginUIState.this::create),
                    new UserInterfaceOption(List.of("n", "no"), "do not create new game", List.of(),
                            (args) -> UserInterfaceCommandOutput.popState("Did not create a new game."))

            );
        }
    }
}
