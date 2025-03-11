package dataaccess;

import dataaccess.mysql.MySqlDataAccess;
import model.AuthData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AuthDAOTests {
    private static AuthDAO authDAO;

    private final AuthData authOne = new AuthData("reallyCleverTokenName", "testUsername");

    private final AuthData authTwo = new AuthData("differentCleverName", "testUsername2");

    @BeforeAll
    public static void beforeAll() throws DataAccessException {
        DataAccess dataAccess = new MySqlDataAccess();
        authDAO = dataAccess.getAuthDAO();
        authDAO.clear();
    }

    @AfterEach
    public void tearDown() throws DataAccessException {
        authDAO.clear();
    }

    @Test
    public void clear() throws DataAccessException {
        authDAO.insertAuth(authOne);
        authDAO.insertAuth(authTwo);

        authDAO.clear();

        Assertions.assertNull(authDAO.findAuth(authOne.authToken()));
        Assertions.assertNull(authDAO.findAuth(authTwo.authToken()));
    }

    @Test
    public void insertPass() throws DataAccessException {
        authDAO.insertAuth(authOne);
        AuthData found = authDAO.findAuth(authOne.authToken());
        Assertions.assertNotNull(found);
        Assertions.assertEquals(authOne, found);
    }

    @Test
    public void insertFail() throws DataAccessException {
        authDAO.insertAuth(authOne);
        Assertions.assertThrows(DataAccessException.class, () -> authDAO.insertAuth(authOne));
    }

    @Test
    public void findPass() throws DataAccessException {
        authDAO.insertAuth(authOne);
        AuthData found = authDAO.findAuth(authOne.authToken());
        Assertions.assertNotNull(found);
        Assertions.assertEquals(authOne, found);
    }

    @Test
    public void findFail() throws DataAccessException {
        Assertions.assertNull(authDAO.findAuth(authOne.authToken()));
    }

    @Test
    public void deletePass() throws DataAccessException {
        authDAO.insertAuth(authOne);
        authDAO.deleteAuth(authOne.authToken());
        Assertions.assertNull(authDAO.findAuth(authOne.authToken()));
    }

    @Test
    public void deleteFail() throws DataAccessException {
        authDAO.insertAuth(authOne);
        authDAO.deleteAuth(authTwo.authToken());
        Assertions.assertNotNull(authDAO.findAuth(authOne.authToken()));
    }
}
