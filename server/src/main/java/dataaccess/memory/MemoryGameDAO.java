package dataaccess.memory;

import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MemoryGameDAO implements GameDAO {
    private final Map<Integer, GameData> games = new HashMap<>();

    @Override
    public void clear() throws DataAccessException {
        games.clear();
    }

    @Override
    public GameData findGame(int gameID) throws DataAccessException {
        return games.get(gameID);
    }

    @Override
    public Collection<GameData> findAllGames() throws DataAccessException {
        return Collections.unmodifiableCollection(games.values());
    }

    @Override
    public GameData insertGame(GameData game) throws DataAccessException {
        if (game.game() == null) {
            throw new DataAccessException("Game cannot be null");
        }
        int gameID = 1;
        while (games.get(gameID) != null) {
            gameID++;
        }
        game = new GameData(gameID, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
        games.put(gameID, game);
        return game;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (!games.containsKey(game.gameID())) {
            throw new DataAccessException("Game does not exist");
        }
        if (game.game() == null) {
            throw new DataAccessException("Game cannot be null");
        }
        games.remove(game.gameID());
        games.put(game.gameID(), game);
    }
}
