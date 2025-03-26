package web;

import serialize.Serializer;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketCommunicator extends Endpoint implements MessageHandler.Whole<String> {

    private final String url;
    private WebsocketObserver observer;

    private Session session;

    public WebSocketCommunicator(String url) {
        this.url = url.replace("http", "ws") + "/ws";
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {}

    @Override
    public void onMessage(String s) {
        try {
            ServerMessage message = Serializer.deserialize(s, ServerMessage.class);
            switch (message.getServerMessageType()) {
                case LOAD_GAME -> observer.loadGame(((LoadGameMessage) message).getGame());
                case NOTIFICATION -> observer.notify(((NotificationMessage) message).getMessage());
                case ERROR -> observer.error(((ErrorMessage) message).getErrorMessage());
            }
        } catch (Exception e) {
            observer.error(e.getMessage());
        }
    }

    void open(WebsocketObserver observer) throws URISyntaxException, DeploymentException, IOException {
        this.observer = observer;
        URI uri = new URI(this.url);
        session = ContainerProvider.getWebSocketContainer().connectToServer(this, uri);
        session.addMessageHandler(String.class, this);
    }

    void close() throws IOException {
        session.close();
        session = null;
        observer = null;
    }

    void sendMessage(UserGameCommand command) throws IOException {
        session.getBasicRemote().sendText(Serializer.serialize(command));
    }
}
