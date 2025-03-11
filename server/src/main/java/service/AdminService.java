package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import exception.ResponseException;

public class AdminService {
    private final DataAccess dataAccess;

    public AdminService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void clear() throws ResponseException {
        try {
            dataAccess.getAuthDAO().clear();
            dataAccess.getGameDAO().clear();
            dataAccess.getUserDAO().clear();
        }catch (DataAccessException e) {
            throw new ResponseException(ResponseException.Reason.INTERNAL_ERROR, e);
        }

    }

}
