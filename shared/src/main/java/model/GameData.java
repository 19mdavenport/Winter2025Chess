package model;

import chess.ChessGame;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
    public GameData withID(int gameID) {
        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
    }

    public GameData withWhiteUsername(String whiteUsername) {
        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
    }

    public GameData withBlackUsername(String blackUsername) {
        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
    }
}
