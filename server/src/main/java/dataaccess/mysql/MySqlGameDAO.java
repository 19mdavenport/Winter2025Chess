package dataaccess.mysql;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import serialize.Serializer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

public class MySqlGameDAO extends MySqlDAO implements GameDAO {
    public MySqlGameDAO() throws DataAccessException {}

    @Override
    public void clear() throws DataAccessException {
        executeUpdate("TRUNCATE TABLE game");
    }

    @Override
    public GameData findGame(int gameID) throws DataAccessException {
        return executeQuery("SELECT * FROM game WHERE id = ?", nullSafeParseSingle(this::readGame), gameID);
    }

    @Override
    public Collection<GameData> findAllGames() throws DataAccessException {
        return executeQuery("SELECT * FROM game", parseCollection(this::readGame));
    }

    @Override
    public GameData insertGame(GameData game) throws DataAccessException {
        int id = executeUpdate("INSERT INTO game (name, white_username, black_username, game) VALUES (?, ?, ?, ?)",
                        game.gameName(), game.whiteUsername(), game.blackUsername(), game.game());
        return game.withID(id);
    }


    @Override
    public void updateGame(GameData game) throws DataAccessException {
        executeUpdate("UPDATE game SET name = ?, white_username = ?, black_username = ?, game = ? WHERE id = ?",
                game.gameName(), game.whiteUsername(), game.blackUsername(), game.game(), game.gameID());
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[]{"""
            CREATE TABLE IF NOT EXISTS game (
                id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
                name VARCHAR(64) NOT NULL,
                white_username VARCHAR(64),
                black_username VARCHAR(64),
                game TEXT NOT NULL
            )
            """};
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        return new GameData(
                rs.getInt("id"),
                rs.getString("white_username"),
                rs.getString("black_username"),
                rs.getString("name"),
                Serializer.deserialize(rs.getString("game"), ChessGame.class)
        );
    }
}
