package web;

import exception.ResponseException;
import model.ErrorResponse;
import serialize.Serializer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

public class HttpCommunicator {

    private final String url;

    public HttpCommunicator(String url) {
        this.url = url;
    }

    private static String readString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStreamReader sr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            char[] buf = new char[1024];
            int len;
            while ((len = sr.read(buf)) > 0) {
                sb.append(buf, 0, len);
            }
            return sb.toString();
        }
    }


    private static void writeString(String str, OutputStream os) throws IOException {
        try (OutputStreamWriter sw = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            sw.write(str);
            sw.flush();
        }
    }


    <T> T execute(String apiEndpoint, String requestMethod, Object request, Map<String, String> headers,
                  Class<T> responseClass) throws ResponseException {
        try {
            boolean requestPresent = request != null;

            HttpURLConnection http = makeConnection(apiEndpoint, requestMethod, headers, requestPresent);

            if (requestPresent) {
                OutputStream reqBody = http.getOutputStream();
                writeString(Serializer.serialize(request), reqBody);
                reqBody.close();
            }

            throwIfNotSuccessful(http);

            String resp = readString(http.getInputStream());
            return responseClass != null ? Serializer.deserialize(resp, responseClass) : null;
        } catch (IOException | URISyntaxException e) {
            throw new ResponseException(ResponseException.Reason.INTERNAL_ERROR, "Error connecting to server", e);
        }
    }

    private HttpURLConnection makeConnection(String apiEndpoint, String requestMethod,
                                             Map<String, String> headers, boolean requestPresent)
            throws URISyntaxException, IOException {
        URL url = new URI(this.url + apiEndpoint).toURL();

        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod(requestMethod.toUpperCase(Locale.ROOT));
        http.setDoOutput(requestPresent);
        http.addRequestProperty("Accept", "application/json");

        for(Map.Entry<String, String> header : headers.entrySet()) {
            http.addRequestProperty(header.getKey(), header.getValue());
        }

        http.connect();
        return http;
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        int status = http.getResponseCode();
        if (status / 100 != 2) {
            String resp = readString(http.getErrorStream());
            ErrorResponse response = Serializer.deserialize(resp, ErrorResponse.class);
            String message;
            if(response == null || (message = response.message()) == null) {
                message = String.format("Response was %d %s", http.getResponseCode(), http.getResponseMessage());
            }
            ResponseException.Reason reason = switch (status) {
                case 400 -> ResponseException.Reason.BAD_INPUT;
                case 401 -> ResponseException.Reason.BAD_AUTH;
                case 403 -> ResponseException.Reason.ITEM_TAKEN;
                default -> ResponseException.Reason.INTERNAL_ERROR;
            };
            throw new ResponseException(reason, message);
        }
    }
}
