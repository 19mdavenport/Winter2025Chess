package handler;

import dataaccess.DataAccess;
import model.GameData;
import service.ChessServerException;
import service.GameService;

public class CreateGameHandler extends HttpHandler<GameData> {

    public CreateGameHandler(DataAccess dataAccess) {
        super(dataAccess);
    }


    @Override
    protected Class<GameData> getRequestClass() {
        return GameData.class;
    }


    @Override
    protected Object getServiceResult(DataAccess dataAccess, GameData request, String authtoken) throws ChessServerException {
        return new GameService(dataAccess).createGame(request, authtoken);
    }

}
