package client;

import exception.ResponseException;
import model.*;
import web.ServerFacade;

import java.util.*;

public class GameIDManager implements ServerFacade {
    private final Map<Integer, Integer> serverToLocal = new HashMap<>();
    private final Map<Integer, Integer> localToServer = new HashMap<>();
    private final ServerFacade delegate;
    private Integer currentServerId;

    public GameIDManager(ServerFacade delegate) {
        this.delegate = delegate;
    }

    @Override
    public AuthData register(UserData request) throws ResponseException {
        return delegate.register(request);
    }

    @Override
    public AuthData login(UserData request) throws ResponseException {
        return delegate.login(request);
    }

    @Override
    public void logout() throws ResponseException {
        delegate.logout();
    }

    public Integer createGame(String gameName) throws ResponseException {
        Integer response = delegate.createGame(gameName);
        return addGame(response);
    }

    @Override
    public ListGamesResponse listGames() throws ResponseException {
        ListGamesResponse serverGames = delegate.listGames();
        List<GameData> outGames = new ArrayList<>(serverGames.games());
        outGames.sort(Comparator.comparingInt(GameData::gameID));
        for (int i = 0, outGamesSize = outGames.size(); i < outGamesSize; i++) {
            GameData game = outGames.get(i);
            int serverId = game.gameID();
            Integer localId = serverToLocal.get(serverId);
            if (localId == null) {
                localId = addGame(serverId);
            }
            outGames.set(i, game.withID(localId));
        }
        return new ListGamesResponse(outGames);
    }

    @Override
    public void joinGame(JoinGameRequest request) throws ResponseException {
        if(localToServer.isEmpty()) {
            listGames();
        }
        currentServerId = getServerId(request.gameID());
        delegate.joinGame(new JoinGameRequest(request.playerColor(), currentServerId));
    }

    @Override
    public void observeGame(int localId) {
        currentServerId = getServerId(localId);
    }

    private int getServerId(int localId) {
        if(localToServer.containsKey(localId)) {
            return localToServer.get(localId);
        } else {
            throw new IllegalArgumentException(localId + " is not a valid game number");
        }
    }

    private int addGame(int serverId) {
        int localId = serverToLocal.size() + 1;
        serverToLocal.put(serverId, localId);
        localToServer.put(localId, serverId);
        return localId;
    }
}
