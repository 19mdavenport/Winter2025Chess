package web;

import chess.ChessMove;
import exception.ResponseException;
import model.*;

public interface ServerFacade {
    AuthData register(UserData request) throws ResponseException;
    AuthData login(UserData request) throws ResponseException;
    void logout() throws ResponseException;
    Integer createGame(String gameName) throws ResponseException;
    ListGamesResponse listGames() throws ResponseException;
    void joinGame(JoinGameRequest request, WebsocketObserver observer) throws ResponseException;
    void observeGame(int gameID, WebsocketObserver observer) throws ResponseException;

    void makeMove(ChessMove move) throws ResponseException;
    void leave() throws ResponseException;
    void resign() throws ResponseException;
}
