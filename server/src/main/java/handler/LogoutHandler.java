package handler;

import dataaccess.DataAccess;
import exception.ResponseException;
import service.UserService;

public class LogoutHandler extends HttpHandler<Void>{

    public LogoutHandler(DataAccess dataAccess) {
        super(dataAccess);
    }


    @Override
    protected Class<Void> getRequestClass() {
        return null;
    }


    @Override
    protected Object getServiceResult(DataAccess dataAccess, Void request, String authtoken) throws ResponseException {
        new UserService(dataAccess).logout(authtoken);
        return null;
    }

}
