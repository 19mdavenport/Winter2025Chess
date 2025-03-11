package dataaccess.mysql;

import chess.ChessGame;
import dataaccess.DataAccessException;
import serialize.Serializer;

import java.sql.*;
import java.util.Collection;
import java.util.HashSet;

public abstract class MySqlDAO {
    private static boolean databaseCreated = false;

    public MySqlDAO() throws DataAccessException {
        configureDatabase();
    }

    protected int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                addParams(ps, params);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException(
                    String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    protected <T> T executeQuery(String statement, ResultSetParser<T> parser, Object... params)
            throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {
            addParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return parser.parseResultSet(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException(
                    String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    private void addParams(PreparedStatement ps, Object[] params) throws SQLException, DataAccessException {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            switch (param) {
                case String p -> ps.setString(i + 1, p);
                case Integer p -> ps.setInt(i + 1, p);
                case ChessGame p -> ps.setString(i + 1, Serializer.serialize(p));
                case null -> ps.setNull(i + 1, Types.NULL);
                default -> throw new DataAccessException("Unexpected data type: " + param.getClass());
            }
        }
    }

    protected void configureDatabase() throws DataAccessException {
        if(!databaseCreated) {
            DatabaseManager.createDatabase();
            databaseCreated = true;
        }
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : getCreateStatements()) {
                try (PreparedStatement preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }

    protected abstract String[] getCreateStatements();

    protected <T> ResultSetParser<T> nullSafeParseSingle(ResultSetParser<T> delegate) {
        return rs -> {
            if (!rs.next()) {
                return null;
            }
            return delegate.parseResultSet(rs);
        };
    }

    protected <T> ResultSetParser<Collection<T>> parseCollection(ResultSetParser<T> delegate) {
        return rs -> {
            Collection<T> coll = new HashSet<>();
            while (rs.next()) {
                coll.add(delegate.parseResultSet(rs));
            }
            return coll;
        };
    }


    @FunctionalInterface
    protected interface ResultSetParser<T> {
        T parseResultSet(ResultSet rs) throws SQLException;
    }
}
