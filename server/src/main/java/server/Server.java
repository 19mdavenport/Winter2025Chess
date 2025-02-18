package server;

import dataaccess.DataAccess;
import dataaccess.memory.MemoryDataAccess;
import handler.*;
import model.GameData;
import model.JoinGameRequest;
import model.UserData;
import service.AdminService;
import service.ChessServerException;
import service.GameService;
import service.UserService;
import spark.Spark;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        DataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);
        GameService gameService = new GameService(dataAccess);
        AdminService adminService = new AdminService(dataAccess);

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", new HttpHandler<>(UserData.class, (r, a) -> userService.register(r)));

        Spark.path("/session", () -> {
            Spark.post("", new HttpHandler<>(UserData.class, (r, a) -> userService.login(r)));
            Spark.delete("", new HttpHandler<>(null, (r, a) -> {
                userService.logout(a);
                return null;
            }));
        });

        Spark.path("/game", () -> {
            Spark.get("", new HttpHandler<>(null, (r, a) -> gameService.listGames(a)));
            Spark.post("", new HttpHandler<>(GameData.class, gameService::createGame));
            Spark.put("", new HttpHandler<>(JoinGameRequest.class, (r, a) -> {
                gameService.joinGame(r, a);
                return null;
            }));
        });

        Spark.delete("/db", new HttpHandler<>(null, (r, a) -> {
            adminService.clear();
            return null;
        }));

        Spark.exception(ChessServerException.class, new ChessServerExceptionHandler());

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
