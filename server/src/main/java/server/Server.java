package server;

import dataaccess.DataAccess;
import dataaccess.memory.MemoryDataAccess;
import handler.*;
import service.BadRequestException;
import service.ChessServerException;
import service.RequestItemTakenException;
import service.UnauthorizedException;
import spark.Spark;

import java.net.HttpURLConnection;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        DataAccess dataAccess = new MemoryDataAccess();

        // Register your endpoints and handle exceptions here.
        Spark.get("/costume", (request, response) ->
                Spark.halt(403, "<html><body><p>You are not authorized to view this costume</p></body></html>"));
        Spark.post("/user", new RegisterHandler(dataAccess));

        Spark.path("/session", () -> {
            Spark.post("", new LoginHandler(dataAccess));
            Spark.delete("", new LogoutHandler(dataAccess));
        });

        Spark.path("/game", () -> {
            Spark.get("", new ListGamesHandler(dataAccess));
            Spark.post("", new CreateGameHandler(dataAccess));
            Spark.put("", new JoinGameHandler(dataAccess));
        });

        Spark.delete("/db", new ClearHandler(dataAccess));

        Spark.exception(BadRequestException.class, new ChessServerExceptionHandler<>(HttpURLConnection.HTTP_BAD_REQUEST));
        Spark.exception(UnauthorizedException.class, new ChessServerExceptionHandler<>(HttpURLConnection.HTTP_UNAUTHORIZED));
        Spark.exception(RequestItemTakenException.class, new ChessServerExceptionHandler<>(HttpURLConnection.HTTP_FORBIDDEN));
        Spark.exception(ChessServerException.class, new ChessServerExceptionHandler<>(HttpURLConnection.HTTP_INTERNAL_ERROR));

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
