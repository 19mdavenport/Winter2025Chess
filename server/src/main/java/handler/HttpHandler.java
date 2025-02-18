package handler;

import serialize.Serializer;
import service.ChessServerException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.net.HttpURLConnection;

public class HttpHandler<T> implements Route {

    private final ChessService<T> service;
    private final Class<T> requestClass;


    public HttpHandler(Class<T> requestClass, ChessService<T> service) {
        this.service = service;
        this.requestClass = requestClass;
    }

    @Override
    public Object handle(Request request, Response response) throws ChessServerException {
        String authToken = request.headers("Authorization");

        T requestObject = null;
        if(requestClass != null) {
            requestObject = Serializer.deserialize(request.body(), requestClass);
        }

        Object result = service.apply(requestObject, authToken);

        response.status(HttpURLConnection.HTTP_OK);
        return Serializer.serialize(result);
    }

    public interface ChessService<T> {
        Object apply(T requestObject, String authToken) throws ChessServerException;
    }

}
