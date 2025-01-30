package handler;

import dataaccess.DataAccess;
import model.UserData;
import service.ChessServerException;
import service.UserService;

public class LoginHandler extends HttpHandler<UserData> {

    public LoginHandler(DataAccess dataAccess) {
        super(dataAccess);
    }


    @Override
    protected Class<UserData> getRequestClass() {
        return UserData.class;
    }


    @Override
    protected Object getServiceResult(DataAccess dataAccess, UserData request, String authtoken) throws ChessServerException {
        return new UserService(dataAccess).login(request);
    }

}
