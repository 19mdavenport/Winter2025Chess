package handler;

import dataaccess.DataAccess;
import exception.ResponseException;
import service.GameService;

public class ListGamesHandler extends HttpHandler<Void>{

    public ListGamesHandler(DataAccess dataAccess) {
        super(dataAccess);
    }


    @Override
    protected Class<Void> getRequestClass() {
        return null;
    }


    @Override
    protected Object getServiceResult(DataAccess dataAccess, Void request, String authtoken) throws ResponseException {
        return new GameService(dataAccess).listGames(authtoken);
    }

}
