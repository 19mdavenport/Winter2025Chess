package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDAO {
    /**
     * Clears the database of all games
     *
     * @throws DataAccessException A DataAccessException is thrown if data cannot be accessed for any reason
     */
    void clear() throws DataAccessException;

    /**
     * Returns the Game matching the provided String
     *
     * @param gameID The Game to look for
     * @return The requested Game, or {@code null} if it couldn't be found
     * @throws DataAccessException A DataAccessException is thrown if data cannot be accessed for any reason
     */
    GameData findGame(int gameID) throws DataAccessException;


    /**
     * Finds and returns all games currently in the database
     *
     * @return A collection containing all current games
     * @throws DataAccessException A DataAccessException is thrown if data cannot be accessed for any reason
     */
    Collection<GameData> findAllGames() throws DataAccessException;


    /**
     * Inserts the provided Game into the database
     *
     * @param game The Game to insert
     * @return The gameID of the inserted game
     * @throws DataAccessException A DataAccessException is thrown if data cannot be accessed for any reason
     */
    GameData insertGame(GameData game) throws DataAccessException;


    /**
     * Updates the game with the provided game's gameID to the provided game
     *
     * @param game Game to update
     * @throws DataAccessException A DataAccessException is thrown if data cannot be accessed for any reason
     */
    void updateGame(GameData game) throws DataAccessException;
}
