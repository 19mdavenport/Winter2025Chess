package dataaccess.mysql;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.UserData;

public class MySqlUserDAO extends MySqlDAO implements UserDAO {
    public MySqlUserDAO() throws DataAccessException {
    }

    @Override
    public void clear() throws DataAccessException {
        executeUpdate("TRUNCATE TABLE user");
    }

    @Override
    public void insertUser(UserData user) throws DataAccessException {
        String statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?);";
        executeUpdate(statement, user.username(), user.password(), user.email());
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return executeQuery("SELECT * FROM user WHERE username=?",
                nullSafeParseSingle((rs) ->
                    new UserData(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email")
                    )
                ),
                username);
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[]{"""
            CREATE TABLE IF NOT EXISTS user (
                username VARCHAR(64) NOT NULL PRIMARY KEY,
                password VARCHAR(64) NOT NULL,
                email VARCHAR(64) NOT NULL
            )
            """};
    }
}
