package com.ptit.socialchat.servlet;

import com.google.gson.Gson;
import com.ptit.socialchat.dao.FriendDAO;
import com.ptit.socialchat.model.Message;
import com.ptit.socialchat.model.User;
import com.ptit.socialchat.service.ChatService;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;
import com.ptit.socialchat.dao.MessageDAO;

public class ChatServlet extends HttpServlet {

    private final ChatService chatService = new ChatService();
    private final FriendDAO friendDAO = new FriendDAO();
    private final MessageDAO messageDAO = new MessageDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        long currentUserId = (long) session.getAttribute("userId");
        String action = req.getParameter("action");

        if ("stats".equals(action)) {
            try {
                long totalUnread = chatService.getTotalUnreadConversations(currentUserId);
                Map<String, Long> unreadCounts = chatService.getUnreadCounts(currentUserId);
                long friendRequestsCount = friendDAO.getPendingRequestCount(currentUserId);
                
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("totalUnread", totalUnread);
                responseData.put("unreadCounts", unreadCounts);
                responseData.put("friendRequestsCount", friendRequestsCount);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write(gson.toJson(responseData));
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return;
        }

        if ("history".equals(action)) {
            // Return JSON chat history between current user and another user
            String otherUsername = req.getParameter("otherUser");
            try {
                long lastMessageId = 0;
                String lastIdStr = req.getParameter("lastMessageId");
                if (lastIdStr != null && !lastIdStr.trim().isEmpty()) {
                    try { lastMessageId = Long.parseLong(lastIdStr); } catch (NumberFormatException ignored) {}
                }
                int limit = 50;

                List<Message> history = chatService.getChatHistory(currentUserId, otherUsername, lastMessageId, limit);
                boolean isFriend = chatService.isFriendWith(currentUserId, otherUsername);

                List<Map<String, Object>> result = new ArrayList<>();
                for (Message msg : history) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", msg.getId());
                    map.put("content", msg.getContent());
                    map.put("timestamp", msg.getTimestamp() != null ? msg.getTimestamp().toString() : "");
                    map.put("senderUsername", msg.getSender().getUsername());
                    map.put("senderFullName", msg.getSender().getFullName());
                    map.put("receiverUsername", msg.getReceiver().getUsername());
                    map.put("imageUrl", msg.getImageUrl() != null ? msg.getImageUrl() : "");
                    map.put("isRead", msg.isRead());
                    // add senderAvatar securely
                    map.put("senderAvatar", msg.getSender().getAvatar() != null ? msg.getSender().getAvatar() : "");
                    map.put("isRecalled", msg.isRecalled());
                    result.add(map);
                }
                
                long lastReadMessageId = chatService.getLastReadMessageId(currentUserId, otherUsername);

                Map<String, Object> responseData = new HashMap<>();
                responseData.put("isFriend", isFriend);
                responseData.put("messages", result);
                responseData.put("lastReadMessageId", lastReadMessageId);

                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write(gson.toJson(responseData));
            } catch (IllegalArgumentException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write(gson.toJson(java.util.Collections.singletonMap("error", e.getMessage())));
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return;
        }

        // Default: show chat.jsp
        List<User> friends = friendDAO.getFriendsByUserId(currentUserId);
        List<User> chattedUsers = messageDAO.getChattedUsers(currentUserId);
        
        // Combine keeping order (friends first) and removing duplicates
        Map<Long, User> contactMap = new LinkedHashMap<>();
        for (User u : friends) {
            contactMap.put(u.getId(), u);
        }
        for (User u : chattedUsers) {
            if (!contactMap.containsKey(u.getId())) {
                contactMap.put(u.getId(), u);
            }
        }
        
        List<User> contacts = new ArrayList<>(contactMap.values());
        req.setAttribute("friends", contacts);
        req.getRequestDispatcher("/WEB-INF/views/chat.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        long currentUserId = (long) session.getAttribute("userId");
        String action = req.getParameter("action");

        if ("send".equals(action)) {
            String receiverUsername = req.getParameter("receiverUsername");
            String content = req.getParameter("content");
            String imageUrl = req.getParameter("imageUrl");
            try {
                chatService.sendMessage(currentUserId, receiverUsername, content, imageUrl);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write(gson.toJson(java.util.Collections.singletonMap("status", "ok")));
            } catch (IllegalArgumentException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write(gson.toJson(java.util.Collections.singletonMap("error", e.getMessage())));
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else if ("markRead".equals(action)) {
            String senderUsername = req.getParameter("senderUsername");
            try {
                chatService.markMessagesAsRead(currentUserId, senderUsername);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write(gson.toJson(java.util.Collections.singletonMap("status", "ok")));
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else if ("clearHistory".equals(action)) {
            String otherUser = req.getParameter("otherUser");
            try {
                chatService.clearChatHistory(currentUserId, otherUser);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write(gson.toJson(java.util.Collections.singletonMap("status", "ok")));
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else if ("recall".equals(action)) {
            String msgIdStr = req.getParameter("messageId");
            try {
                long messageId = Long.parseLong(msgIdStr);
                chatService.recallMessage(messageId, currentUserId);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write(gson.toJson(java.util.Collections.singletonMap("status", "ok")));
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            resp.sendError(400, "Unknown action");
        }
    }
}
