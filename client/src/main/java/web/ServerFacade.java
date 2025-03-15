package web;

import exception.ResponseException;
import model.*;

public interface ServerFacade {
    AuthData register(UserData request) throws ResponseException;
    AuthData login(UserData request) throws ResponseException;
    void logout() throws ResponseException;
    Integer createGame(String gameName) throws ResponseException;
    ListGamesResponse listGames() throws ResponseException;
    void joinGame(JoinGameRequest request) throws ResponseException;
    void observeGame(int gameID);
}
