package web;

import exception.ResponseException;
import model.*;

import java.util.Map;

public class ServerFacade {

    private final HttpCommunicator http;
    private String authToken;


    public ServerFacade(String url) {
        this.http = new HttpCommunicator(url);
    }

    public AuthData register(UserData request) throws ResponseException {
        return authenticate(request, "/user");
    }

    public AuthData login(UserData request) throws ResponseException {
        return authenticate(request, "/session");
    }

    private AuthData authenticate(UserData request, String apiEndpoint) throws ResponseException {
        AuthData out = http.execute(apiEndpoint, "POST", request, Map.of(), AuthData.class);
        this.authToken = out.authToken();
        return out;
    }

    public void logout() throws ResponseException {
        http.execute("/session", "DELETE", null, authHeaderMap(), null);
    }

    public GameData createGame(GameData request) throws ResponseException {
        return http.execute("/game", "POST", request, authHeaderMap(), GameData.class);
    }

    public ListGamesResponse listGames() throws ResponseException {
        return http.execute("/game", "GET", null, authHeaderMap(), ListGamesResponse.class);
    }

    public void joinGame(JoinGameRequest request) throws ResponseException {
        http.execute("/game", "PUT", request, authHeaderMap(), null);
    }

    private Map<String, String> authHeaderMap() {
        return Map.of("Authorization", authToken);
    }


}
