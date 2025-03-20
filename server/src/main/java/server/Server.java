package server;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.memory.MemoryDataAccess;
import dataaccess.mysql.MySqlDataAccess;
import handler.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import exception.ResponseException;
import spark.Spark;
import websocket.WebSocketHandler;

public class Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        DataAccess dataAccess = createDataAccess();

        Spark.webSocket("/ws", new WebSocketHandler(dataAccess));

        // Register your endpoints and handle exceptions here.
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

        Spark.exception(ResponseException.class, new ChessServerExceptionHandler());

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private DataAccess createDataAccess() {
        try {
            return new MySqlDataAccess();
        } catch (DataAccessException e) {
            LOGGER.warn("Couldn't instantiate MySQL storage, falling back on memory storage:\n{}", e.getMessage(), e);
            return new MemoryDataAccess();
        }
    }
}
