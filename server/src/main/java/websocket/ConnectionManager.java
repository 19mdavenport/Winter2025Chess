package websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serialize.Serializer;
import websocket.messages.ErrorMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);
    private static final ByteBuffer PING_BUFFER = ByteBuffer.wrap("PING".getBytes());

    private final Map<Integer, Set<Session>> sessions = new ConcurrentHashMap<>();

    public ConnectionManager() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new PingClientTask(), 10000, 10000);
    }

    public void addSession(Session session) {
        addSession(0, session);
    }

    public void addSession(int gameId, Session session) {
        if(!sessions.containsKey(gameId)) {
            sessions.put(gameId, Collections.synchronizedSet(new HashSet<>()));
        }
        sessions.get(0).remove(session);
        sessions.get(gameId).add(session);
    }

    public void removeSession(Session session) {
        if(sessions.get(0).contains(session)) {
            sessions.get(0).remove(session);
        }
        else {
            for (Set<Session> set : sessions.values()) {
                set.remove(session);
            }
        }
    }

    public void removeSession(int gameId, Session session) {
        if(sessions.containsKey(gameId)) {
            sessions.get(gameId).remove(session);
        }
        sessions.get(0).add(session);
    }

    public void broadcast(String message, int gameId, Session exclude) throws IOException {
        for (Session ses : sessions.get(gameId)) {
            if (ses != exclude) {
                sendMessage(ses, message);
            }
        }
    }

    public void sendError(Session session, String message) throws IOException {
        sendMessage(session, Serializer.serialize(new ErrorMessage(message)));
    }

    public void sendMessage(Session session, String message) throws IOException {
        if (session.isOpen()) {
            session.getRemote().sendString(message);
        }
    }

    public void clear() {
        for (Set<Session> set : sessions.values()) {
            for (Session session : set) {
                if(session.isOpen()) {
                    session.close();
                }
            }
        }
        sessions.clear();
    }

    private class PingClientTask extends TimerTask {
        @Override
        public void run() {
            for (Set<Session> set : sessions.values()) {
                for (Session session : set) {
                    if(session.isOpen()) {
                        try {
                            session.getRemote().sendPing(PING_BUFFER);
                        } catch (IOException e) {
                            LOGGER.warn("Error sending ping", e);
                        }
                    }
                }
            }
        }
    }
}
