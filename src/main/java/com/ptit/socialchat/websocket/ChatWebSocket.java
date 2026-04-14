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

    // Manage all active sessions mapping userId -> Set of Sessions
    private static final Map<Long, java.util.Set<Session>> userSessions = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    @OnOpen
    public void onOpen(Session session) {
        Long userId = (Long) session.getUserProperties().get("userId");
        if (userId != null) {
            userSessions.computeIfAbsent(userId, k -> java.util.Collections.newSetFromMap(new ConcurrentHashMap<>())).add(session);
        } else {
            // Defer closing the session to prevent IllegalStateException in Tomcat's doOnOpen
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                    session.close(new javax.websocket.CloseReason(javax.websocket.CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthorized"));
                } catch (Exception ignored) {}
            }).start();
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
            java.util.Set<Session> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Handle error, optionally remove session
        Long userId = (Long) session.getUserProperties().get("userId");
        if (userId != null) {
            java.util.Set<Session> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
        }
        try {
            session.close();
        } catch (IOException ignored) {}
    }

    /**
     * Broadcast a JSON event to a specific user id (to all their active sessions).
     */
    public static void sendToUser(long userId, Object payload) {
        java.util.Set<Session> sessions = userSessions.get(userId);
        if (sessions != null && !sessions.isEmpty()) {
            String json = gson.toJson(payload);
            java.util.List<Session> deadSessions = new java.util.ArrayList<>();
            
            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(json);
                    } catch (IOException e) {
                        deadSessions.add(session);
                    }
                } else {
                    deadSessions.add(session);
                }
            }
            
            if (!deadSessions.isEmpty()) {
                sessions.removeAll(deadSessions);
            }
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
        }
    }
}
