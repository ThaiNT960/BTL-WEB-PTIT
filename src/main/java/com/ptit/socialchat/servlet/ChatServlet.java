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

public class ChatServlet extends HttpServlet {

    private final ChatService chatService = new ChatService();
    private final FriendDAO friendDAO = new FriendDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        long currentUserId = (long) session.getAttribute("userId");
        String action = req.getParameter("action");

        if ("history".equals(action)) {
            // Return JSON chat history between current user and another user
            String otherUsername = req.getParameter("otherUser");
            try {
                List<Message> history = chatService.getChatHistory(currentUserId, otherUsername);
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
                    result.add(map);
                }
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write(gson.toJson(result));
            } catch (IllegalArgumentException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json;charset=UTF-8");
                // Import Collections dynamically if needed, but easier to use map or simple JSON string
                resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return;
        }

        // Default: show chat.jsp
        List<User> friends = friendDAO.getFriendsByUserId(currentUserId);
        req.setAttribute("friends", friends);
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
                resp.getWriter().write("{\"status\":\"ok\"}");
            } catch (IllegalArgumentException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            resp.sendError(400, "Unknown action");
        }
    }
}
