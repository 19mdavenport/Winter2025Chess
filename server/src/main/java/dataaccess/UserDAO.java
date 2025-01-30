package dataaccess;

import model.UserData;

public interface UserDAO {
    /**
     * Clears the database of all users
     *
     * @throws DataAccessException A DataAccessException is thrown if data cannot be accessed for any reason
     */
    void clear() throws DataAccessException;


    /**
     * Inserts the provided User into the database
     *
     * @param user The User to insert
     * @throws DataAccessException A DataAccessException is thrown if data cannot be accessed for any reason
     */
    void insertUser(UserData user) throws DataAccessException;


    UserData getUser(String username) throws DataAccessException;
}
