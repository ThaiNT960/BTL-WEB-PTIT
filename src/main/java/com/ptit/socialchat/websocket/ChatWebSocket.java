package com.ptit.socialchat.websocket;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/ws/chat", configurator = WsConfigurator.class)
public class ChatWebSocket {

    // Manage all active sessions mapping userId -> Session
    private static final Map<Long, Session> userSessions = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    @OnOpen
    public void onOpen(Session session) {
        Long userId = (Long) session.getUserProperties().get("userId");
        if (userId != null) {
            userSessions.put(userId, session);
        } else {
            try {
                session.close();
            } catch (IOException ignored) {}
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // We mainly use WS to push events from Server to Client
        // But if client sends JSON, we can handle it here (e.g. typing indicators)
        // System.out.println("Received: " + message);
    }

    @OnClose
    public void onClose(Session session) {
        Long userId = (Long) session.getUserProperties().get("userId");
        if (userId != null) {
            userSessions.remove(userId);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Handle error, optionally remove session
        Long userId = (Long) session.getUserProperties().get("userId");
        if (userId != null) {
            userSessions.remove(userId);
        }
        try {
            session.close();
        } catch (IOException ignored) {}
    }

    /**
     * Broadcast a JSON event to a specific user id.
     */
    public static void sendToUser(long userId, Object payload) {
        Session session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(gson.toJson(payload));
            } catch (IOException e) {
                userSessions.remove(userId);
            }
        }
    }
}
