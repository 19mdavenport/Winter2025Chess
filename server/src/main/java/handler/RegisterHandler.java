package handler;

import dataaccess.DataAccess;
import model.AuthData;
import model.UserData;
import service.ChessServerException;
import service.UserService;

public class RegisterHandler extends HttpHandler<UserData> {

    public RegisterHandler(DataAccess dataAccess) {
        super(dataAccess);
    }


    @Override
    protected Class<UserData> getRequestClass() {
        return UserData.class;
    }


    @Override
    protected AuthData getServiceResult(DataAccess dataAccess, UserData user, String authtoken) throws ChessServerException {
        return new UserService(dataAccess).register(user);
    }

}
