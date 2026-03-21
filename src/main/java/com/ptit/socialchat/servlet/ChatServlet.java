package com.ptit.socialchat.servlet;

import com.google.gson.Gson;
import com.ptit.socialchat.dao.FriendDAO;
import com.ptit.socialchat.dao.MessageDAO;
import com.ptit.socialchat.dao.UserDAO;
import com.ptit.socialchat.model.Message;
import com.ptit.socialchat.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

public class ChatServlet extends HttpServlet {

    private final MessageDAO messageDAO = new MessageDAO();
    private final FriendDAO friendDAO = new FriendDAO();
    private final UserDAO userDAO = new UserDAO();
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
            User otherUser = userDAO.findByUsername(otherUsername);
            if (otherUser == null) {
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("[]");
                return;
            }
            List<Message> history = messageDAO.getChatHistory(currentUserId, otherUser.getId());
            List<Map<String, Object>> result = new ArrayList<>();
            for (Message msg : history) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", msg.getId());
                map.put("content", msg.getContent());
                map.put("timestamp", msg.getTimestamp());
                map.put("senderUsername", msg.getSender().getUsername());
                map.put("senderFullName", msg.getSender().getFullName());
                map.put("receiverUsername", msg.getReceiver().getUsername());
                result.add(map);
            }
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(gson.toJson(result));
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
        long currentUserId = (long) session.getAttribute("userId");
        String action = req.getParameter("action");

        if ("send".equals(action)) {
            String receiverUsername = req.getParameter("receiverUsername");
            String content = req.getParameter("content");
            if (content == null || content.trim().isEmpty() || receiverUsername == null) {
                resp.sendError(400, "Missing parameters");
                return;
            }
            User receiver = userDAO.findByUsername(receiverUsername);
            if (receiver == null) {
                resp.sendError(404, "User not found");
                return;
            }
            messageDAO.save(currentUserId, receiver.getId(), content.trim());
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"status\":\"ok\"}");
        } else {
            resp.sendError(400, "Unknown action");
        }
    }
}
