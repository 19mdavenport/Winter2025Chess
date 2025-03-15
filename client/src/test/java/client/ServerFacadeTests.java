package client;

import chess.ChessGame;
import exception.ResponseException;
import model.AuthData;
import model.JoinGameRequest;
import model.ListGamesResponse;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import web.ServerFacade;
import web.ServerFacadeImpl;

import java.util.UUID;


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
        facade = new ServerFacadeImpl("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @BeforeEach
    public void register() throws ResponseException {
        username = "user" + UUID.randomUUID();
        UserData request = new UserData(username, PASSWORD, EMAIL);
        authToken = facade.register(request).authToken();
    }


    @Test
    public void registerPass() throws ResponseException {
        String otherUsername = username;
        while (username.equals(otherUsername)) {
            otherUsername = "user" + UUID.randomUUID();
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
        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> facade.register(request));
        Assertions.assertEquals(ResponseException.Reason.ITEM_TAKEN, e.getReason());
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
            otherUsername = "user" + UUID.randomUUID();
        }

        UserData request = new UserData(otherUsername, PASSWORD, null);
        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> facade.login(request));
        Assertions.assertEquals(ResponseException.Reason.BAD_AUTH, e.getReason());
    }


    @Test
    public void logoutPass() throws ResponseException {
        facade.logout();
        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> facade.listGames());
        Assertions.assertEquals(ResponseException.Reason.BAD_AUTH, e.getReason());
    }


    @Test
    public void logoutFail() throws ResponseException {
        facade.logout();
        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> facade.logout());
        Assertions.assertEquals(ResponseException.Reason.BAD_AUTH, e.getReason());
    }


    @Test
    public void createGamePass() throws ResponseException {
        String gameName = "game" + UUID.randomUUID();
        Integer result = facade.createGame(gameName);

        Assertions.assertNotNull(result);

        Assertions.assertTrue(result > 0);
    }


    @Test
    public void createGameFail() {
        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> facade.createGame(null));
        Assertions.assertEquals(ResponseException.Reason.BAD_INPUT, e.getReason());
    }


    @Test
    public void joinGamePass() throws ResponseException {
        String gameName = "game" + UUID.randomUUID();
        Integer cGResult = facade.createGame(gameName);

        JoinGameRequest request = new JoinGameRequest(ChessGame.TeamColor.BLACK, cGResult);
        Assertions.assertDoesNotThrow(() -> facade.joinGame(request));
    }


    @Test
    public void joinGameFail() {
        JoinGameRequest request = new JoinGameRequest(ChessGame.TeamColor.WHITE, 123456789);
        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> facade.joinGame(request));
        Assertions.assertEquals(ResponseException.Reason.BAD_INPUT, e.getReason());
    }


    @Test
    public void listGamesPass() throws ResponseException {
        String gameName = "game" + UUID.randomUUID();
        int gameID = facade.createGame(gameName);
        ListGamesResponse result = facade.listGames();

        Assertions.assertNotNull(result);

        Assertions.assertFalse(result.games().isEmpty());
        Assertions.assertTrue(result.games().stream().anyMatch(gameData -> gameData.gameID() == gameID));
    }


    @Test
    public void listGamesFail() throws ResponseException {
        facade.logout();
        ResponseException e = Assertions.assertThrows(ResponseException.class, () -> facade.listGames());
        Assertions.assertEquals(ResponseException.Reason.BAD_AUTH, e.getReason());
    }

}
