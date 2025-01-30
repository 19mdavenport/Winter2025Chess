package dataaccess;

import model.AuthData;

public interface AuthDAO {
    /**
     * Clears the database of all AuthTokens
     *
     * @throws DataAccessException A DataAccessException is thrown if data cannot be accessed for any reason
     */
    void clear() throws DataAccessException;
    /**
     * Inserts the provided AuthToken into the database
     *
     * @param authToken The AuthToken to insert
     * @throws DataAccessException A DataAccessException is thrown if data cannot be accessed for any reason
     */
    void insertAuth(AuthData authToken) throws DataAccessException;


    /**
     * Returns the AuthToken matching the provided String
     *
     * @param authToken The AuthToken to look for
     * @return The requested AuthToken, or {@code null} if it couldn't be found
     * @throws DataAccessException A DataAccessException is thrown if data cannot be accessed for any reason
     */
    AuthData findAuth(String authToken) throws DataAccessException;


    /**
     * Deletes the AuthToken matching the provided String from the database
     *
     * @param authToken The AuthToken to delete
     * @throws DataAccessException A DataAccessException is thrown if data cannot be accessed for any reason
     */
    void deleteAuth(String authToken) throws DataAccessException;
}
