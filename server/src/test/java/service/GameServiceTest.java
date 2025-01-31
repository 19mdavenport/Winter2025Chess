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

    @BeforeAll
    public static void beforeAll() throws ChessServerException {
        dataAccess = new MemoryDataAccess();
        authDAO = dataAccess.getAuthDAO();
        gameDAO = dataAccess.getGameDAO();
        userDAO = dataAccess.getUserDAO();
    }


    @BeforeEach
    public void setUp() throws ChessServerException {
        new AdminService(dataAccess).clear();
    }


    @Test
    public void createGamePass() throws ChessServerException, DataAccessException {

        AuthData token = new AuthData("totallyRandomAuth", "testusername");

        authDAO.insertAuth(token);

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
    public void createGameFail() throws ChessServerException {

        GameData request = new GameData(0, null, null, "Super Exciting Chess Game Failure!", new ChessGame());

        Assertions.assertThrows(UnauthorizedException.class,
                () -> new GameService(dataAccess).createGame(request, null));
    }


    @Test
    public void listGamesPass() throws ChessServerException, DataAccessException {

        UserData user = new UserData("sheila", "superSecurePa$$w0rd", "noreply@byu.edu");

        userDAO.insertUser(user);

        GameData game = new GameData(0, user.username(), null, "Really Cool Name", new ChessGame());
        game = gameDAO.insertGame(game);

        AuthData token = new AuthData("totallyRandomAuth", user.username());
        authDAO.insertAuth(token);

        ListGamesResponse result = new GameService(dataAccess).listGames(token.authToken());

        Assertions.assertEquals(1, result.games().size());

        GameData foundGame = result.games().iterator().next();

        Assertions.assertEquals(game.gameName(), foundGame.gameName());
        Assertions.assertEquals(game.gameID(), foundGame.gameID());

        Assertions.assertEquals(user.username(), foundGame.whiteUsername());
        Assertions.assertNull(foundGame.blackUsername());

    }


    @Test
    public void listGamesFail() {
        Assertions.assertThrows(UnauthorizedException.class,
                () -> new GameService(dataAccess).listGames(UUID.randomUUID().toString()));

    }


    @Test
    public void joinGamePass() throws ChessServerException, DataAccessException {

        UserData user = new UserData("sheila", "superSecurePa$$w0rd", "noreply@byu.edu");

        userDAO.insertUser(user);

        GameData game = new GameData(0, null, null, "Really Cool Name", new ChessGame());
        game = gameDAO.insertGame(game);

        AuthData token = new AuthData("totallyRandomAuth", user.username());
        authDAO.insertAuth(token);


        JoinGameRequest request = new JoinGameRequest(ChessGame.TeamColor.WHITE, game.gameID());

        Assertions.assertDoesNotThrow(() -> new GameService(dataAccess).joinGame(request, token.authToken()));

        GameData foundGameData = gameDAO.findGame(game.gameID());

        Assertions.assertEquals(game.gameName(), foundGameData.gameName());
        Assertions.assertEquals(game.gameID(), foundGameData.gameID());
        Assertions.assertNull(foundGameData.blackUsername());
        Assertions.assertEquals(user.username(), foundGameData.whiteUsername());

    }


    @Test
    public void joinGameFail() {

        JoinGameRequest request = new JoinGameRequest(ChessGame.TeamColor.WHITE, -1);

        Assertions.assertThrows(BadRequestException.class,
                () -> new GameService(dataAccess).joinGame(request, "Invalid token"));

    }

}
