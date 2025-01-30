package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;

public class AdminService {
    private final DataAccess dataAccess;

    public AdminService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void clear() throws ChessServerException {
        try {
            dataAccess.getAuthDAO().clear();
            dataAccess.getGameDAO().clear();
            dataAccess.getUserDAO().clear();
        }catch (DataAccessException e) {
            throw new ChessServerException(e);
        }

    }

}
