package dataaccess;

import dataaccess.mysql.MySqlDataAccess;
import model.UserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class UserDAOTests {
    private static UserDAO userDAO;

    private final UserData userOne = new UserData("mdaven19", "notGonnaTe11Yo0", "mdaven19@byu.edu");

    private final UserData userTwo = new UserData("19mdavenport", "n0tG0nnaTe11Yo0", "19mdavenport@gmail.com");

    @BeforeAll
    public static void beforeAll() throws DataAccessException {
        DataAccess dataAccess = new MySqlDataAccess();
        userDAO = dataAccess.getUserDAO();
        userDAO.clear();
    }

    @AfterEach
    public void tearDown() throws DataAccessException {
        userDAO.clear();
    }

    @Test
    public void clearAllUser() throws DataAccessException {
        userDAO.insertUser(userOne);
        userDAO.insertUser(userTwo);
        userDAO.clear();
        Assertions.assertNull(userDAO.getUser(userOne.username()));
        Assertions.assertNull(userDAO.getUser(userTwo.username()));
    }

    @Test
    public void insertPass() throws DataAccessException {
        userDAO.insertUser(userOne);
        Assertions.assertNotNull(userDAO.getUser(userOne.username()));
    }

    @Test
    public void insertFail() throws DataAccessException {
        userDAO.insertUser(userOne);
        Assertions.assertThrows(DataAccessException.class, () -> userDAO.insertUser(userOne));
    }

    @Test
    public void getUserPass() throws DataAccessException {
        userDAO.insertUser(userOne);
        Assertions.assertEquals(userOne, userDAO.getUser(userOne.username()));
    }

    @Test
    public void getUserFail() throws DataAccessException {
        Assertions.assertNull(userDAO.getUser("testUsername2"));
    }
}
