package dataaccess.mysql;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;

public class MySqlAuthDAO extends MySqlDAO implements AuthDAO {
    public MySqlAuthDAO() throws DataAccessException {}

    @Override
    public void clear() throws DataAccessException {
        executeUpdate("TRUNCATE TABLE auth;");
    }

    @Override
    public void insertAuth(AuthData authData) throws DataAccessException {
        String statement = "INSERT INTO auth (token, username) VALUES (?, ?)";
        executeUpdate(statement, authData.authToken(), authData.username());
    }

    @Override
    public AuthData findAuth(String authToken) throws DataAccessException {
        return executeQuery("SELECT * FROM auth WHERE token = ?",
                nullSafeParseSingle(rs ->
                    new AuthData(
                        rs.getString("token"),
                        rs.getString("username")
                    )
                ),
                authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        executeUpdate("DELETE FROM auth WHERE token = ?", authToken);
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[]{"""
            CREATE TABLE IF NOT EXISTS auth (
                token VARCHAR(64) NOT NULL PRIMARY KEY,
                username VARCHAR(64) NOT NULL
            )
            """};
    }
}
