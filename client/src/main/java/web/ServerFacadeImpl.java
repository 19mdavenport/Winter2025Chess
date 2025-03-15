package web;

import exception.ResponseException;
import model.*;

import java.util.Map;

public class ServerFacadeImpl implements ServerFacade {

    private final HttpCommunicator http;
    private String authToken;

    public ServerFacadeImpl(String url) {
        this.http = new HttpCommunicator(url);
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
    public void joinGame(JoinGameRequest request) throws ResponseException {
        http.execute("/game", "PUT", request, authHeaderMap(), null);
    }

    @Override
    public void observeGame(int gameID) {

    }

    private Map<String, String> authHeaderMap() {
        return Map.of("Authorization", authToken);
    }


}
