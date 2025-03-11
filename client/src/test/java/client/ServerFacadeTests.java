package client;

import chess.ChessGame;
import model.*;
import org.junit.jupiter.api.*;
import server.Server;
import exception.ResponseException;
import web.ServerFacade;

import java.util.Random;


public class ServerFacadeTests {
    private static final String PASSWORD = "password";

    private static final String EMAIL = "no-reply@byu.edu";

    private static Server server;

    private static ServerFacade facade;

    private static String username;

    private String authToken;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @BeforeEach
    public void register() throws ResponseException {
        username = "user" + new Random().nextInt(9999);
        UserData request = new UserData(username, PASSWORD, EMAIL);
        authToken = facade.register(request).authToken();
    }


    @Test
    public void registerPass() throws ResponseException {
        String otherUsername = username;
        while (username.equals(otherUsername)) {
            otherUsername = "user" + new Random().nextInt(9999);
        }

        UserData request = new UserData(otherUsername, PASSWORD, EMAIL);
        AuthData result = facade.register(request);

        Assertions.assertNotNull(result);

        Assertions.assertNotNull(result.authToken());
        Assertions.assertNotEquals(authToken, result.authToken());
    }


    @Test
    public void registerFail() {
        UserData request = new UserData(username, PASSWORD, EMAIL);
        Assertions.assertThrows(ResponseException.class, () -> facade.register(request));
    }


    @Test
    public void loginPass() throws ResponseException {
        UserData request = new UserData(username, PASSWORD, null);
        AuthData result = facade.login(request);

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.authToken());
        Assertions.assertNotEquals(authToken, result.authToken());
    }


    @Test
    public void loginFail() {
        String otherUsername = username;
        while (username.equals(otherUsername)) {
            otherUsername = "user" + new Random().nextInt(9999);
        }

        UserData request = new UserData(otherUsername, PASSWORD, null);
        Assertions.assertThrows(ResponseException.class, () -> facade.login(request));

    }


    @Test
    public void logoutPass() throws ResponseException {
        facade.logout();
        Assertions.assertThrows(ResponseException.class, () -> facade.listGames());
    }


    @Test
    public void logoutFail() throws ResponseException {
        facade.logout();
        Assertions.assertThrows(ResponseException.class, () -> facade.logout());
    }


    @Test
    public void createGamePass() throws ResponseException {
        String gameName = "game" + new Random().nextInt(9999);
        GameData request = new GameData(0, null, null, gameName, null);
        GameData result = facade.createGame(request);

        Assertions.assertNotNull(result);

        Assertions.assertEquals(request.gameName(), result.gameName());
        Assertions.assertTrue(result.gameID() > 0);
    }


    @Test
    public void createGameFail() {
        GameData request = new GameData(0, null, null, null, null);
        Assertions.assertThrows(ResponseException.class, () -> facade.createGame(request));
    }


    @Test
    public void joinGamePass() throws ResponseException {
        String gameName = "game" + new Random().nextInt(9999);
        GameData cGRequest = new GameData(0, null, null, gameName, null);
        GameData cGResult = facade.createGame(cGRequest);

        JoinGameRequest request = new JoinGameRequest(ChessGame.TeamColor.BLACK, cGResult.gameID());
        Assertions.assertDoesNotThrow(() -> facade.joinGame(request));
    }


    @Test
    public void joinGameFail() {
        JoinGameRequest request = new JoinGameRequest(ChessGame.TeamColor.WHITE, 123456789);
        Assertions.assertThrows(ResponseException.class, () -> facade.joinGame(request));
    }


    @Test
    public void listGamesPass() throws ResponseException {
        String gameName = "game" + new Random().nextInt(9999);
        GameData request = new GameData(0, null, null, gameName, null);
        facade.createGame(request);
        ListGamesResponse result = facade.listGames();

        Assertions.assertNotNull(result);

        Assertions.assertFalse(result.games().isEmpty());
    }


    @Test
    public void listGamesFail() throws ResponseException {
        facade.logout();
        Assertions.assertThrows(ResponseException.class, () -> facade.listGames());
    }

}
