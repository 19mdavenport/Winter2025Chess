package websocket;

import chess.ChessGame;
import chess.InvalidMoveException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serialize.Serializer;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Objects;

@WebSocket
public class WebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketHandler.class);

    private final ConnectionManager connectionManager = new ConnectionManager();

    private final DataAccess dataAccess;

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @OnWebSocketConnect
    public void connect(Session session) {
        connectionManager.addSession(session);
    }

    @OnWebSocketClose
    public void close(Session session, int statusCode, String reason) {
        connectionManager.removeSession(session);
    }

    @OnWebSocketError
    public void error(Throwable error) {
        if (!((error instanceof EofException) || error.getCause() != null && error.getCause().getMessage() != null &&
                error.getCause().getMessage().contains("Connection reset by peer"))) {
            LOGGER.warn("WebSocket error: ", error);
        }
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        try {
            LOGGER.debug("Received from {}: {}", session.getRemoteAddress(), message);
            UserGameCommand command = Serializer.deserialize(message, UserGameCommand.class);

            AuthData token = dataAccess.getAuthDAO().findAuth(command.getAuthToken());
            if (token == null) {
                throw new WebsocketException("Invalid authtoken");
            }

            GameData game = dataAccess.getGameDAO().findGame(command.getGameID());
            if (game == null) {
                throw new WebsocketException("Invalid gameID");
            }

            switch (command.getCommandType()) {
                case CONNECT -> connect(session, token.username(), game);
                case MAKE_MOVE -> makeMove(session, (MakeMoveCommand) command, token.username(), game);
                case LEAVE -> leave(session, token.username(), game);
                case RESIGN -> resign(session, token.username(), game);
            }
        } catch (WebsocketException e) {
            sendError(session, "Error: " + e.getMessage(), message);
        } catch (Throwable t) {
            LOGGER.warn("{}: ", t.getClass().getName(), t);
            sendError(session, "Error: Unknown server error occurred: " + t.getMessage(), message);
        }
    }

    private void sendError(Session session, String message, String input) throws IOException {
        connectionManager.sendError(session, message);
        LOGGER.info("Error to {}: Error message: {} Input was: {}", session.getRemoteAddress(), message, input);
    }

    private void connect(Session session, String username, GameData game) throws IOException {
        connectionManager.addSession(game.gameID(), session);

        ServerMessage loadGame = new LoadGameMessage(game.game());
        String loadGameJson = Serializer.serialize(loadGame);
        connectionManager.sendMessage(session, loadGameJson);

        String message = String.format("User %s is now %s.", username, positionInGame(username, game));
        ServerMessage notify = new NotificationMessage(message);
        String notifyJson = Serializer.serialize(notify);
        connectionManager.broadcast(notifyJson, game.gameID(), session);
    }

    private void leave(Session session, String username, GameData game) throws IOException, DataAccessException {
        connectionManager.removeSession(game.gameID(), session);

        String message = String.format("User %s is no longer %s", username, positionInGame(username, game));
        ServerMessage notify = new NotificationMessage(message);
        String notifyJson = Serializer.serialize(notify);
        connectionManager.broadcast(notifyJson, game.gameID(), session);

        if (username.equals(game.whiteUsername())) {
            game = new GameData(game.gameID(), null, game.blackUsername(), game.gameName(), game.game());
            dataAccess.getGameDAO().updateGame(game);
        } else if (username.equals(game.blackUsername())) {
            game = new GameData(game.gameID(), game.whiteUsername(), null, game.gameName(), game.game());
            dataAccess.getGameDAO().updateGame(game);
        }
    }

    private void makeMove(Session session, MakeMoveCommand command, String username, GameData game)
            throws IOException, WebsocketException, DataAccessException {
        if (command.getMove() == null) {
            throw new WebsocketException("Must include a move");
        }
        confirmGameStatus(game, username);
        if ((game.game().getTeamTurn() == ChessGame.TeamColor.WHITE && !Objects.equals(game.whiteUsername(), username)) ||
                (game.game().getTeamTurn() == ChessGame.TeamColor.BLACK &&
                        !Objects.equals(game.blackUsername(), username))) {
            throw new WebsocketException("It's not your turn");
        }

        try {
            game.game().makeMove(command.getMove());
            dataAccess.getGameDAO().updateGame(game);
        } catch (InvalidMoveException e) {
            throw new WebsocketException("That's not a valid move: " + e.getMessage());
        }

        ServerMessage loadGame = new LoadGameMessage(game.game());
        ServerMessage notify = new NotificationMessage(String.format("%s makes move %s", username, command.getMove()));

        String loadGameJson = Serializer.serialize(loadGame);
        String notifyJson = Serializer.serialize(notify);

        connectionManager.broadcast(loadGameJson, game.gameID(), null);
        connectionManager.broadcast(notifyJson, game.gameID(), session);

        checkGameStatus(username, game);
    }

    private void checkGameStatus(String username, GameData game) throws IOException, DataAccessException {
        String extra = null;
        boolean gameOver = false;
        if (game.game().isInCheckmate(game.game().getTeamTurn())) {
            extra = String.format("checkmate. %s wins!", username);
            gameOver = true;
        } else if (game.game().isInStalemate(game.game().getTeamTurn())) {
            extra = "stalemate. The game ends in a draw";
            gameOver = true;
        } else if (game.game().isInCheck(game.game().getTeamTurn())) {
            extra = "check.";
        }
        if (extra != null) {
            String opponenet = opponentUsername(username, game);
            String message = String.format("%s's move places %s in %s", username, opponenet, extra);
            ServerMessage notify = new NotificationMessage(message);
            String notifyJson = Serializer.serialize(notify);
            connectionManager.broadcast(notifyJson, game.gameID(), null);
            if(gameOver) {
                game.game().setActive(false);
            }
            dataAccess.getGameDAO().updateGame(game);
        }
    }

    private void resign(Session session, String username, GameData game)
            throws IOException, WebsocketException, DataAccessException {
        confirmGameStatus(game, username);

        game.game().setActive(false);
        dataAccess.getGameDAO().updateGame(game);

        String opponent = opponentUsername(username, game);
        String message = String.format("%s has resigned. %s wins!", username, opponent);
        ServerMessage notify = new NotificationMessage(message);
        String notifyJson = Serializer.serialize(notify);
        connectionManager.broadcast(notifyJson, game.gameID(), session);

        notify = new NotificationMessage(String.format("You(%s) have resigned. %s wins.", username, opponent));
        notifyJson = Serializer.serialize(notify);
        connectionManager.sendMessage(session, notifyJson);
    }

    private void confirmGameStatus(GameData game, String username) throws WebsocketException {
        if (!(Objects.equals(game.blackUsername(), username) || Objects.equals(game.whiteUsername(), username))) {
            throw new WebsocketException("You are not a player in this game");
        }

        if (!game.game().isActive()) {
            throw new WebsocketException("Game is over");
        }
    }

    private String opponentUsername(String username, GameData game) {
        return (Objects.equals(username, game.whiteUsername())) ? game.blackUsername() : game.whiteUsername();
    }

    private String positionInGame(String username, GameData game) {
        if (username.equals(game.whiteUsername())) {
            return "playing as white";
        } else if (username.equals(game.blackUsername())) {
            return "playing as black";
        } else {
            return "watching";
        }
    }

    public void clear() {
        connectionManager.clear();
    }
}
