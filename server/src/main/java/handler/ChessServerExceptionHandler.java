package handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serialize.Serializer;
import service.ChessServerException;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;

import java.util.Map;

public class ChessServerExceptionHandler implements ExceptionHandler<ChessServerException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChessServerExceptionHandler.class);

    @Override
    public void handle(ChessServerException e, Request request, Response response) {
        LOGGER.debug("Exception in {} {}", request.requestMethod(), request.pathInfo(), e);
        response.status(switch (e.getReason()) {
            case BAD_INPUT -> 400;
            case BAD_AUTH -> 401;
            case ITEM_TAKEN -> 403;
            case INTERNAL_ERROR -> 500;
        });
        response.body(Serializer.serialize(Map.of("message", e.getMessage())));
    }
}
