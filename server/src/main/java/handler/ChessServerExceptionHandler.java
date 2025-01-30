package handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serialize.Serializer;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;

import java.util.Map;


/**
 * Handles Exceptions thrown from the server
 *
 * @param <T> The type of exception
 */
public class ChessServerExceptionHandler<T extends Exception> implements ExceptionHandler<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChessServerExceptionHandler.class);

    private final int responseCode;


    /**
     * @param responseCode HTTP response code to use for this type of Exception
     */
    public ChessServerExceptionHandler(int responseCode) {
        this.responseCode = responseCode;
    }


    @Override
    public void handle(T t, Request request, Response response) {
        LOGGER.debug("Exception in {} {}", request.requestMethod(), request.pathInfo(), t);
        response.status(responseCode);
        response.body(Serializer.serialize(Map.of("message", t.getMessage())));
    }
}
