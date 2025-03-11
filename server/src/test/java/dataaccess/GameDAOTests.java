package dataaccess;

import chess.ChessGame;
import dataaccess.mysql.MySqlDataAccess;
import model.GameData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;

public class GameDAOTests {
    private static GameDAO gameDAO;

    private final GameData gameOne = new GameData(-1, null, null, "superCoolGameName", new ChessGame());

    private final GameData gameTwo = new GameData(-1, null, null, "secondCoolName", new ChessGame());

    @BeforeAll
    public static void beforeAll() throws DataAccessException {
        DataAccess dataAccess = new MySqlDataAccess();
        gameDAO = dataAccess.getGameDAO();
        gameDAO.clear();
    }

    @AfterEach
    public void tearDown() throws DataAccessException {
        gameDAO.clear();
    }

    @Test
    public void clear() throws DataAccessException {
        GameData insGameOne = gameDAO.insertGame(gameOne);
        GameData insGameTwo = gameDAO.insertGame(gameTwo);

        gameDAO.clear();

        Assertions.assertNull(gameDAO.findGame(insGameOne.gameID()));
        Assertions.assertNull(gameDAO.findGame(insGameTwo.gameID()));
    }

    @Test
    public void findAllFail() throws DataAccessException {
        Collection<GameData> games = gameDAO.findAllGames();
        Assertions.assertEquals(0, games.size());
    }

    @Test
    public void findAllPass() throws DataAccessException {
        GameData insGameOne = gameDAO.insertGame(gameOne);
        GameData insGameTwo = gameDAO.insertGame(gameTwo);

        Assertions.assertEquals(gameOne.game(), insGameOne.game());
        Assertions.assertEquals(gameOne.gameName(), insGameOne.gameName());
        Assertions.assertEquals(gameOne.whiteUsername(), insGameOne.whiteUsername());
        Assertions.assertEquals(gameOne.blackUsername(), insGameOne.blackUsername());

        Assertions.assertEquals(gameTwo.game(), insGameTwo.game());
        Assertions.assertEquals(gameTwo.gameName(), insGameTwo.gameName());
        Assertions.assertEquals(gameTwo.whiteUsername(), insGameTwo.whiteUsername());
        Assertions.assertEquals(gameTwo.blackUsername(), insGameTwo.blackUsername());

        Collection<GameData> games = gameDAO.findAllGames();

        Assertions.assertEquals(2, games.size());
        Assertions.assertTrue(games.contains(insGameOne));
        Assertions.assertTrue(games.contains(insGameTwo));
    }

    @Test
    public void findFail() throws DataAccessException {
        Assertions.assertNull(gameDAO.findGame(17));
    }

    @Test
    public void findPass() throws DataAccessException {
        GameData insGameOne = gameDAO.insertGame(gameOne);

        GameData found = gameDAO.findGame(insGameOne.gameID());
        Assertions.assertNotNull(found);
        Assertions.assertEquals(insGameOne, found);
    }

    @Test
    public void insertFail() throws DataAccessException {
        Assertions.assertThrows(DataAccessException.class,
                () -> gameDAO.insertGame(new GameData(-1, null, null, null, null)));
    }

    @Test
    public void insertPass() throws DataAccessException {
        GameData insGameOne = gameDAO.insertGame(gameOne);

        GameData found = gameDAO.findGame(insGameOne.gameID());

        Assertions.assertEquals(gameOne.game(), insGameOne.game());
        Assertions.assertEquals(gameOne.gameName(), insGameOne.gameName());
        Assertions.assertEquals(gameOne.whiteUsername(), insGameOne.whiteUsername());
        Assertions.assertEquals(gameOne.blackUsername(), insGameOne.blackUsername());

        Assertions.assertEquals(insGameOne, found);
    }

    @Test
    public void updateFail() throws DataAccessException {
        GameData insGameOne = gameDAO.insertGame(gameOne);

        Assertions.assertThrows(DataAccessException.class,
                () -> gameDAO.updateGame(new GameData(insGameOne.gameID(), null, null, null, null)));
    }

    @Test
    public void updatePass() throws DataAccessException {
        GameData insGameOne = gameDAO.insertGame(gameOne);

        GameData toUpdate = new GameData(insGameOne.gameID(), "asdf", insGameOne.blackUsername(), insGameOne.gameName(),
                insGameOne.game());
        gameDAO.updateGame(toUpdate);

        GameData found = gameDAO.findGame(toUpdate.gameID());
        Assertions.assertNotNull(found);
        Assertions.assertEquals(toUpdate, found);
    }
}
