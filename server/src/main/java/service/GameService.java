package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.JoinGameRequest;
import model.ListGamesResponse;

public class GameService {

    private final DataAccess dataAccess;


    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }


    public GameData createGame(GameData request, String authToken) throws ChessServerException {
        try {
            authorization(authToken);

            if (request.gameName() == null) {
                throw new BadRequestException("Game name cannot be null");
            }

            GameData game = new GameData(0, null, null, request.gameName(), new ChessGame());
            game = dataAccess.getGameDAO().insertGame(game);

            return game;
        } catch (DataAccessException e) {
            throw new ChessServerException(e);
        }
    }


    public ListGamesResponse listGames(String authToken) throws ChessServerException {
        try {
            authorization(authToken);

            return new ListGamesResponse(dataAccess.getGameDAO().findAllGames());
        } catch (DataAccessException e) {
            throw new ChessServerException(e);
        }
    }


    public synchronized void joinGame(JoinGameRequest request, String authToken) throws ChessServerException {
        try {
            GameData game = dataAccess.getGameDAO().findGame(request.gameID());
            if (game == null) {
                throw new BadRequestException("Error: Game not found");
            }
            if (request.playerColor() == null) {
                throw new BadRequestException("Error: Not a valid color");
            }

            AuthData auth = authorization(authToken);

            if (request.playerColor() == ChessGame.TeamColor.WHITE && game.whiteUsername() != null &&
                    !game.whiteUsername().equals(auth.username()) ||
                    request.playerColor() == ChessGame.TeamColor.BLACK && game.blackUsername() != null &&
                            !game.blackUsername().equals(auth.username())) {
                throw new RequestItemTakenException("Error: Player color taken");
            }

            if (request.playerColor() == ChessGame.TeamColor.WHITE) {
                game = new GameData(game.gameID(), auth.username(), game.blackUsername(), game.gameName(), game.game());
            }
            if (request.playerColor() == ChessGame.TeamColor.BLACK) {
                game = new GameData(game.gameID(), game.whiteUsername(), auth.username(), game.gameName(), game.game());
            }

            dataAccess.getGameDAO().updateGame(game);
        } catch (DataAccessException e) {
            throw new ChessServerException(e);
        }
    }


    private AuthData authorization(String authtoken) throws ChessServerException {
        try {
            AuthData auth = dataAccess.getAuthDAO().findAuth(authtoken);
            if (auth == null) {
                throw new UnauthorizedException("Error: Unauthorized");
            }
            return auth;
        } catch (DataAccessException e) {
            throw new ChessServerException(e);
        }
    }

}
