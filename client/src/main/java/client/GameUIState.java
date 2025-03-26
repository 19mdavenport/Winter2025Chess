package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import exception.ResponseException;
import ui.*;
import web.ServerFacade;
import web.WebsocketObserver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GameUIState extends UserInterfaceState implements WebsocketObserver {
    private static final String PROMPT_TEXT = "Chess Game >> ";
    private final boolean isPlayer;
    private final ServerFacade server;
    private final BoardPrinter boardPrinter;

    private ChessGame game;

    public GameUIState(boolean isPlayer, ServerFacade server, BoardPrinter boardPrinter) {
        super(PROMPT_TEXT, false);
        this.isPlayer = isPlayer;
        this.server = server;
        this.boardPrinter = boardPrinter;
    }

//    @Override
//    public String help() {
//        return """
//                Options:
//                Highlight legal moves: "hl", "highlight" <position> (e.g. f5)
//                Make a move: "m", "move", "make" <source> <destination> <optional promotion>(e.g. f5 e4 q)
//                Redraw Chess Board: "r", "redraw"
//                Change color scheme: "c", "colors" <color number> (enter no number to enter color scheme creator)
//                Resign from game: "res", "resign"
//                Leave game: "leave"
//                Print this message: "h", "help"
//                """;
//    }

    @Override
    protected Collection<UserInterfaceOption> createOptions() {
        List<UserInterfaceOption> options = new ArrayList<>();
        options.add(new UserInterfaceOption(List.of("hl", "highlight"), "Highlight legal moves",
                List.of(new CommandArgument<>("position to highlight", ChessPosition.class)), this::highlight));

        if (isPlayer) {
            options.add(new UserInterfaceOption(List.of("m", "move, make"), "Make a move",
                    List.of(new CommandArgument<>("start position", ChessPosition.class),
                            new CommandArgument<>("end position", ChessPosition.class),
                            new CommandArgument<>("promotion piece", ChessPiece.PieceType.class,
                                    args -> {
                                        var v = game.validMoves((ChessPosition) args[0]);
                                        var str = v.stream().filter(m -> m.getEndPosition().equals(args[1]));
                                        return str.anyMatch(m -> m.getPromotionPiece() != null);
                                    }, null)),
                    this::makeMove));
        }
        
        options.add(new UserInterfaceOption(List.of("r", "redraw"), "Redraw Chess Board", 
                List.of(), (args) -> redraw()));
        
        //TODO: colors
        
        if (isPlayer) {
            options.add(new UserInterfaceOption(List.of("res", "resign"), "Resign from game", 
                    List.of(), (args) -> resign()));
        }
        
        options.add(new UserInterfaceOption(List.of("l", "leave"), isPlayer ? "Leave this game" : "stop observing this game",
                List.of(), (args) -> leave()));

        return options;
    }

    @Override
    public String getPromptText() {
        return "Chess Game";
    }

    private UserInterfaceCommandOutput highlight(Object[] args) {
        boardPrinter.highlightMoves((ChessPosition) args[0]);
        return UserInterfaceCommandOutput.success("");
    }

    private UserInterfaceCommandOutput makeMove(Object[] args) throws ResponseException {
        server.makeMove(new ChessMove((ChessPosition) args[0], (ChessPosition) args[1], (ChessPiece.PieceType) args[2]));
        return UserInterfaceCommandOutput.success("");
    }

    private UserInterfaceCommandOutput redraw() {
        boardPrinter.printGame();
        return UserInterfaceCommandOutput.success("");
    }

//    private UserInterfaceCommandOutput colors(String[] args) {
//        if (args.length != 1) {
//            new ColorSchemeCreator().createColorScheme();
//            return UserInterfaceCommandOutput.asdf("", true);
//        }
//        try {
//            int newColor = Integer.parseInt(args[0]);
//            if (newColor < 1) {
//                return UserInterfaceCommandOutput.asdf("color number cannot be less than 1", false);
//            }
//            int max = ChessBoardColorScheme.COLOR_SCHEMES.size();
//            if (newColor > max) {
//                return UserInterfaceCommandOutput.asdf("color number cannot be greater than %d".formatted(max), false);
//            }
//            DataCache.getInstance().setColorScheme(ChessBoardColorScheme.COLOR_SCHEMES.get(newColor - 1));
//            return UserInterfaceCommandOutput.asdf("Color scheme set to scheme %d".formatted(newColor), true);
//        } catch (NumberFormatException e) {
//            return UserInterfaceCommandOutput.asdf("could not parse %s as a number".formatted(args[0]), false);
//        }
//    }

    private UserInterfaceCommandOutput resign() {
        return UserInterfaceCommandOutput.newState("", new SingleUseUIState("Are you sure you want to resign? [y/n]") {
            @Override
            protected Collection<UserInterfaceOption> createSingleUseOptions() {
                return List.of(
                        new UserInterfaceOption(List.of("y", "yes"), "create a new game of chess",
                                List.of(), args -> {
                                    server.resign();
                                    return UserInterfaceCommandOutput.success("");
                                }),
                        new UserInterfaceOption(List.of("n", "no"), "do not create new game", List.of(),
                                (args) -> UserInterfaceCommandOutput.success("Did not resign."))
                );
            }
        });
    }

    private UserInterfaceCommandOutput leave() throws ResponseException {
        server.leave();
        return UserInterfaceCommandOutput.popState("You have left the game");
    }

    @Override
    public void loadGame(ChessGame game) {
        this.game = game;
        boardPrinter.printNewGame(game);
        reprompt();
    }

    @Override
    public void notify(String message) {
        System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_GREEN + message + EscapeSequences.RESET_TEXT_COLOR);
        reprompt();
    }

    @Override
    public void error(String message) {
        System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_RED + message + EscapeSequences.RESET_TEXT_COLOR);
        reprompt();
    }

    private void reprompt() {
        System.out.print(PROMPT_TEXT);
    }
}
