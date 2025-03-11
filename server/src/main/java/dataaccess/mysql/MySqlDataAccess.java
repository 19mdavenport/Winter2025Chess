package dataaccess.mysql;

import dataaccess.*;

public class MySqlDataAccess implements DataAccess {

    private final AuthDAO authDAO;

    private final GameDAO gameDAO;

    private final UserDAO userDAO;

    public MySqlDataAccess() throws DataAccessException {
        authDAO = new MySqlAuthDAO();
        gameDAO = new MySqlGameDAO();
        userDAO = new MySqlUserDAO();
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
