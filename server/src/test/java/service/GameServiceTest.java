package service;

import chess.ChessGame;
import dataaccess.*;
import dataaccess.memory.MemoryDataAccess;
import model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class GameServiceTest {

    private static DataAccess dataAccess;
    private static AuthDAO authDAO;
    private static GameDAO gameDAO;
    private static UserDAO userDAO;
    private UserData user;
    private GameData game;
    private AuthData token;

    @BeforeAll
    public static void beforeAll() {
        dataAccess = new MemoryDataAccess();
        authDAO = dataAccess.getAuthDAO();
        gameDAO = dataAccess.getGameDAO();
        userDAO = dataAccess.getUserDAO();
    }


    @BeforeEach
    public void setUp() throws ChessServerException, DataAccessException {
        new AdminService(dataAccess).clear();

        user = new UserData("sheila", "superSecurePa$$w0rd", "noreply@byu.edu");
        userDAO.insertUser(user);
        game = new GameData(0, null, null, "Really Cool Name", new ChessGame());
        game = gameDAO.insertGame(game);
        token = new AuthData("totallyRandomAuth", user.username());
        authDAO.insertAuth(token);
    }


    @Test
    public void createGamePass() throws ChessServerException, DataAccessException {
        GameData request = new GameData(0, null, null, "Super Exciting Chess Game!", new ChessGame());
        GameData result = new GameService(dataAccess).createGame(request, token.authToken());
        Assertions.assertTrue(result.gameID() >= 0);

        GameData game = gameDAO.findGame(result.gameID());
        Assertions.assertEquals(request.gameName(), game.gameName());
        Assertions.assertNull(game.blackUsername());
        Assertions.assertNull(game.whiteUsername());
        Assertions.assertNotNull(game.game());
    }


    @Test
    public void createGameBadAuth() {
        GameData request = new GameData(0, null, null, "Super Exciting Chess Game Failure!", new ChessGame());
        ChessServerException e = Assertions.assertThrows(ChessServerException.class,
                () -> new GameService(dataAccess).createGame(request, null));
        Assertions.assertEquals(ChessServerException.Reason.BAD_AUTH, e.getReason());
    }

    @Test
    public void createGameBadInput() {
        GameData[] requests = new GameData[]{
                null,
                new GameData(0, null, null, null, null)
        };
        for (GameData request : requests) {
            ChessServerException e = Assertions.assertThrows(ChessServerException.class,
                    () -> new GameService(dataAccess).createGame(request, token.authToken()));
            Assertions.assertEquals(ChessServerException.Reason.BAD_INPUT, e.getReason());
        }
    }


    @Test
    public void listGamesPass() throws ChessServerException {
        ListGamesResponse result = new GameService(dataAccess).listGames(token.authToken());
        Assertions.assertEquals(1, result.games().size());

        GameData foundGame = result.games().iterator().next();
        Assertions.assertEquals(game.gameName(), foundGame.gameName());
        Assertions.assertEquals(game.gameID(), foundGame.gameID());
        Assertions.assertNull(foundGame.whiteUsername());
        Assertions.assertNull(foundGame.blackUsername());
    }


    @Test
    public void listGamesFail() {
        ChessServerException e = Assertions.assertThrows(ChessServerException.class,
                () -> new GameService(dataAccess).listGames(UUID.randomUUID().toString()));
        Assertions.assertEquals(ChessServerException.Reason.BAD_AUTH, e.getReason());
    }


    @Test
    public void joinGameWhite() throws DataAccessException {
        JoinGameRequest request = new JoinGameRequest(ChessGame.TeamColor.WHITE, game.gameID());
        Assertions.assertDoesNotThrow(() -> new GameService(dataAccess).joinGame(request, token.authToken()));

        GameData foundGameData = gameDAO.findGame(game.gameID());
        Assertions.assertEquals(game.gameName(), foundGameData.gameName());
        Assertions.assertEquals(game.gameID(), foundGameData.gameID());
        Assertions.assertNull(foundGameData.blackUsername());
        Assertions.assertEquals(user.username(), foundGameData.whiteUsername());
    }

    @Test
    public void joinGameBlack() throws DataAccessException {
        JoinGameRequest request = new JoinGameRequest(ChessGame.TeamColor.BLACK, game.gameID());
        Assertions.assertDoesNotThrow(() -> new GameService(dataAccess).joinGame(request, token.authToken()));

        GameData foundGameData = gameDAO.findGame(game.gameID());
        Assertions.assertEquals(game.gameName(), foundGameData.gameName());
        Assertions.assertEquals(game.gameID(), foundGameData.gameID());
        Assertions.assertNull(foundGameData.whiteUsername());
        Assertions.assertEquals(user.username(), foundGameData.blackUsername());
    }


    @Test
    public void joinGameBadInput() {
        JoinGameRequest[] requests = new JoinGameRequest[]{
                null,
                new JoinGameRequest(ChessGame.TeamColor.WHITE, -1),
                new JoinGameRequest(null, game.gameID())
        };
        for (JoinGameRequest request : requests) {
            ChessServerException e = Assertions.assertThrows(ChessServerException.class,
                    () -> new GameService(dataAccess).joinGame(request, token.authToken()));
            Assertions.assertEquals(ChessServerException.Reason.BAD_INPUT, e.getReason());
        }
    }

    @Test
    public void joinGameStealSpot() throws DataAccessException {
        UserData user2 = new UserData("sheila2", "otherPass", "email@example.com");
        userDAO.insertUser(user2);
        AuthData token2 = new AuthData("totallyRandomAuth2", user2.username());
        authDAO.insertAuth(token2);

        for(ChessGame.TeamColor color : ChessGame.TeamColor.values()) {
            JoinGameRequest request = new JoinGameRequest(color, game.gameID());
            Assertions.assertDoesNotThrow(() -> new GameService(dataAccess).joinGame(request, token.authToken()));
            ChessServerException e = Assertions.assertThrows(ChessServerException.class,
                    () -> new GameService(dataAccess).joinGame(request, token2.authToken()));
            Assertions.assertEquals(ChessServerException.Reason.ITEM_TAKEN, e.getReason());
        }
    }

    @Test
    public void rejoinGameWhite() throws DataAccessException, ChessServerException {
        JoinGameRequest request = new JoinGameRequest(ChessGame.TeamColor.WHITE, game.gameID());
        new GameService(dataAccess).joinGame(request, token.authToken());
        Assertions.assertDoesNotThrow(() -> new GameService(dataAccess).joinGame(request, token.authToken()));

        GameData foundGameData = gameDAO.findGame(game.gameID());
        Assertions.assertEquals(game.gameName(), foundGameData.gameName());
        Assertions.assertEquals(game.gameID(), foundGameData.gameID());
        Assertions.assertNull(foundGameData.blackUsername());
        Assertions.assertEquals(user.username(), foundGameData.whiteUsername());
    }

    @Test
    public void rejoinGameBlack() throws DataAccessException, ChessServerException {
        JoinGameRequest request = new JoinGameRequest(ChessGame.TeamColor.BLACK, game.gameID());
        new GameService(dataAccess).joinGame(request, token.authToken());
        Assertions.assertDoesNotThrow(() -> new GameService(dataAccess).joinGame(request, token.authToken()));

        GameData foundGameData = gameDAO.findGame(game.gameID());
        Assertions.assertEquals(game.gameName(), foundGameData.gameName());
        Assertions.assertEquals(game.gameID(), foundGameData.gameID());
        Assertions.assertNull(foundGameData.whiteUsername());
        Assertions.assertEquals(user.username(), foundGameData.blackUsername());
    }

}
