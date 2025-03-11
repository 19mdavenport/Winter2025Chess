package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {

    private final DataAccess dataAccess;


    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }


    public AuthData register(UserData user) throws ChessServerException {
        try {
            if (user == null || user.username() == null || user.password() == null || user.email() == null) {
                throw new ChessServerException(ChessServerException.Reason.BAD_INPUT, "Error: Username, Password, and email must not be null");
            }


            if (dataAccess.getUserDAO().getUser(user.username()) != null) {
                throw new ChessServerException(ChessServerException.Reason.ITEM_TAKEN, "Error: username taken");
            }

            String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
            UserData hashedPasswordUser = new UserData(user.username(), hashedPassword, user.email());

            dataAccess.getUserDAO().insertUser(hashedPasswordUser);

            AuthData auth = AuthData.getNewAuthData(user.username());
            dataAccess.getAuthDAO().insertAuth(auth);

            return auth;
        } catch (DataAccessException e) {
            throw new ChessServerException(ChessServerException.Reason.INTERNAL_ERROR, e);
        }
    }


    public AuthData login(UserData user) throws ChessServerException {
        try {
            UserData foundUser = dataAccess.getUserDAO().getUser(user.username());
            if (foundUser == null || !BCrypt.checkpw(user.password(), foundUser.password())) {
                throw new ChessServerException(ChessServerException.Reason.BAD_AUTH, "Error: Incorrect username or password");
            }

            AuthData auth = AuthData.getNewAuthData(user.username());
            dataAccess.getAuthDAO().insertAuth(auth);

            return auth;
        } catch (DataAccessException e) {
            throw new ChessServerException(ChessServerException.Reason.INTERNAL_ERROR, e);
        }
    }


    public void logout(String authtoken) throws ChessServerException {
        try {
            AuthData delete = dataAccess.getAuthDAO().findAuth(authtoken);
            if (delete == null) {
                throw new ChessServerException(ChessServerException.Reason.BAD_AUTH, "Error: Unauthorized");
            }
            dataAccess.getAuthDAO().deleteAuth(authtoken);
        } catch (DataAccessException e) {
            throw new ChessServerException(ChessServerException.Reason.INTERNAL_ERROR, e);
        }
    }

}
