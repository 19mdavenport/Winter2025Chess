package dataaccess;

public interface DataAccess {
    AuthDAO getAuthDAO();
    GameDAO getGameDAO();
    UserDAO getUserDAO();
}
