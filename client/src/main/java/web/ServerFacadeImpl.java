package web;

import chess.ChessMove;
import exception.ResponseException;
import model.*;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

public class ServerFacadeImpl implements ServerFacade {

    private final HttpCommunicator http;
    private final WebSocketCommunicator websocket;
    private String authToken;
    private Integer gameID;

    public ServerFacadeImpl(String url) {
        this.http = new HttpCommunicator(url);
        this.websocket = new WebSocketCommunicator(url);
    }

    @Override
    public AuthData register(UserData request) throws ResponseException {
        return authenticate(request, "/user");
    }

    @Override
    public AuthData login(UserData request) throws ResponseException {
        return authenticate(request, "/session");
    }

    private AuthData authenticate(UserData request, String apiEndpoint) throws ResponseException {
        AuthData out = http.execute(apiEndpoint, "POST", request, Map.of(), AuthData.class);
        this.authToken = out.authToken();
        return out;
    }

    @Override
    public void logout() throws ResponseException {
        http.execute("/session", "DELETE", null, authHeaderMap(), null);
        this.authToken = null;
    }

    @Override
    public Integer createGame(String gameName) throws ResponseException {
        var request = new GameData(0, null, null, gameName, null);
        return http.execute("/game", "POST", request, authHeaderMap(), GameData.class).gameID();
    }

    @Override
    public ListGamesResponse listGames() throws ResponseException {
        return http.execute("/game", "GET", null, authHeaderMap(), ListGamesResponse.class);
    }

    @Override
    public void joinGame(JoinGameRequest request, WebsocketObserver observer) throws ResponseException {
        http.execute("/game", "PUT", request, authHeaderMap(), null);
        this.gameID = request.gameID();
        connect(observer);
    }

    @Override
    public void observeGame(int gameID, WebsocketObserver observer) throws ResponseException {
        this.gameID = gameID;
        connect(observer);
    }

    private Map<String, String> authHeaderMap() {
        return Map.of("Authorization", authToken);
    }


    private void connect(WebsocketObserver observer) throws ResponseException {
        try {
            websocket.open(observer);
        } catch (URISyntaxException | DeploymentException | IOException e) {
            throw new ResponseException(ResponseException.Reason.INTERNAL_ERROR, e);
        }
        sendMessage(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID));
    }

    public void makeMove(ChessMove move) throws ResponseException {
        sendMessage(new MakeMoveCommand(authToken, gameID, move));
    }

    public void leave() throws ResponseException {
        sendMessage(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID));
        try {
            websocket.close();
            gameID = null;
        } catch (IOException e) {
            throw new ResponseException(ResponseException.Reason.INTERNAL_ERROR, e);
        }
    }

    public void resign() throws ResponseException {
        sendMessage(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID));
    }

    private void sendMessage(UserGameCommand command) throws ResponseException {
        try {
            websocket.sendMessage(command);
        } catch (IOException e) {
            throw new ResponseException(ResponseException.Reason.INTERNAL_ERROR, e);
        }
    }


}
