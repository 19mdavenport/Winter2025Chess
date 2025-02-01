package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import dataaccess.memory.MemoryDataAccess;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserServiceTest {

    private static DataAccess dataAccess;
    private static AuthDAO authDAO;
    private static UserDAO userDAO;


    @BeforeAll
    public static void beforeAll() {
        dataAccess = new MemoryDataAccess();
        userDAO = dataAccess.getUserDAO();
        authDAO = dataAccess.getAuthDAO();
    }


    @BeforeEach
    public void setUp() throws ChessServerException {
        new AdminService(dataAccess).clear();
    }


    @Test
    public void registerPass() throws ChessServerException, DataAccessException {
        UserData request = new UserData("SuperUniqueusername", "SuperSecurePa$$w0rd", "noreply@byu.edu");

        AuthData result = new UserService(dataAccess).register(request);

        Assertions.assertNotNull(result.authToken());
        Assertions.assertEquals(request.username(), result.username());

        AuthData token = authDAO.findAuth(result.authToken());
        Assertions.assertEquals(token, result);

        UserData foundUser = userDAO.getUser(request.username());
        Assertions.assertEquals(request.username(), foundUser.username());
        Assertions.assertEquals(request.email(), foundUser.email());
//        Assertions.assertTrue(BCrypt.checkpw(request.password(), foundUser.password()));
        Assertions.assertEquals(request.password(), foundUser.password());
    }


    @Test
    public void registerTwice() throws ChessServerException {
        UserData request = new UserData("SuperUniqueusername", "SuperSecurePa$$w0rd", "noreply@byu.edu");
        new UserService(dataAccess).register(request);
        ChessServerException e = Assertions.assertThrows(ChessServerException.class,
                () -> new UserService(dataAccess).register(request));
        Assertions.assertEquals(ChessServerException.Reason.ITEM_TAKEN, e.getReason());
    }

    @Test
    public void registerBadInput() throws ChessServerException {
        UserData[] requests = new UserData[]{
                null,
                new UserData(null, "SuperSecurePa$$w0rd", "noreply@byu.edu"),
                new UserData("SuperUniqueusername", null, "noreply@byu.edu"),
                new UserData("SuperUniqueusername", "SuperSecurePa$$w0rd", null)
        };
        for (UserData request : requests) {
            ChessServerException e = Assertions.assertThrows(ChessServerException.class,
                    () -> new UserService(dataAccess).register(request));
            Assertions.assertEquals(ChessServerException.Reason.BAD_INPUT, e.getReason());
        }
    }


    @Test
    public void loginPass() throws ChessServerException, DataAccessException {
        UserData request = new UserData("SuperUniqueusername", "SuperSecurePa$$w0rd", "noreply@byu.edu");
        UserService userService = new UserService(dataAccess);
        userService.register(request);

        AuthData result = userService.login(request);
        Assertions.assertNotNull(result.authToken());
        Assertions.assertEquals(request.username(), result.username());

        AuthData token = authDAO.findAuth(result.authToken());
        Assertions.assertEquals(request.username(), token.username());
    }


    @Test
    public void loginBadUsername() {
        UserData request = new UserData("SuperUniqueusername", "SuperSecurePa$$w0rd", null);
        ChessServerException e = Assertions.assertThrows(ChessServerException.class,
                () -> new UserService(dataAccess).login(request));
        Assertions.assertEquals(ChessServerException.Reason.BAD_AUTH, e.getReason());
    }

    @Test
    public void loginBadPassword() throws DataAccessException {
        UserData user = new UserData("SuperUniqueusername", "SuperSecurePa$$w0rd", "noreply@byu.edu");
        dataAccess.getUserDAO().insertUser(user);

        UserData request = new UserData("SuperUniqueusername", "WrongPass", null);
        ChessServerException e = Assertions.assertThrows(ChessServerException.class,
                () -> new UserService(dataAccess).login(request));
        Assertions.assertEquals(ChessServerException.Reason.BAD_AUTH, e.getReason());
    }


    @Test
    public void logoutPass() throws ChessServerException, DataAccessException {
        UserData request = new UserData("SuperUniqueusername", "SuperSecurePa$$w0rd", "noreply@byu.edu");
        UserService userService = new UserService(dataAccess);
        AuthData registerResult = userService.register(request);

        userService.logout(registerResult.authToken());

        AuthData token = authDAO.findAuth(registerResult.authToken());
        Assertions.assertNull(token);
    }


    @Test
    public void logoutFail() {
        ChessServerException e = Assertions.assertThrows(ChessServerException.class,
                () -> new UserService(dataAccess).logout("Invalid token"));
        Assertions.assertEquals(ChessServerException.Reason.BAD_AUTH, e.getReason());
    }

}
