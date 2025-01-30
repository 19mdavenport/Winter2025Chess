package handler;

import dataaccess.DataAccess;
import model.JoinGameRequest;
import service.ChessServerException;
import service.GameService;

public class JoinGameHandler extends HttpHandler<JoinGameRequest> {

    public JoinGameHandler(DataAccess dataAccess) {
        super(dataAccess);
    }


    @Override
    protected Class<JoinGameRequest> getRequestClass() {
        return JoinGameRequest.class;
    }


    @Override
    protected Object getServiceResult(DataAccess dataAccess, JoinGameRequest request, String authtoken)
            throws ChessServerException {
        new GameService(dataAccess).joinGame(request, authtoken);
        return null;
    }

}
