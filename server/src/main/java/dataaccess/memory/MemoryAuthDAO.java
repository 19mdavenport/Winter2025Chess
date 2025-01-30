package dataaccess.memory;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;

import java.util.HashMap;
import java.util.Map;

public class MemoryAuthDAO implements AuthDAO {
    private final Map<String, AuthData> authTokens = new HashMap<>();
    @Override
    public void clear() throws DataAccessException {
        authTokens.clear();
    }

    @Override
    public void insertAuth(AuthData authToken) throws DataAccessException {
        if (authTokens.containsKey(authToken.authToken())) {
            throw new DataAccessException("Authtoken already exists");
        }
        authTokens.put(authToken.authToken(), authToken);
    }

    @Override
    public AuthData findAuth(String authToken) throws DataAccessException {
        return authTokens.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        authTokens.remove(authToken);
    }
}
