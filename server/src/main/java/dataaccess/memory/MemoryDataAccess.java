package dataaccess.memory;

import dataaccess.AuthDAO;
import dataaccess.DataAccess;
import dataaccess.GameDAO;
import dataaccess.UserDAO;

public class MemoryDataAccess implements DataAccess {
    private final AuthDAO authDAO;

    private final GameDAO gameDAO;

    private final UserDAO userDAO;

    public MemoryDataAccess() {
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        userDAO = new MemoryUserDAO();
    }


    @Override
    public AuthDAO getAuthDAO() {
        return authDAO;
    }

    @Override
    public GameDAO getGameDAO() {
        return gameDAO;
    }

    @Override
    public UserDAO getUserDAO() {
        return userDAO;
    }
}
