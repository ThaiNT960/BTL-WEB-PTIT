package com.ptit.socialchat.servlet;

import com.google.gson.Gson;
import com.ptit.socialchat.dao.FriendDAO;
import com.ptit.socialchat.dao.UserDAO;
import com.ptit.socialchat.model.FriendRequest;
import com.ptit.socialchat.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

public class FriendServlet extends HttpServlet {

    private final FriendDAO friendDAO = new FriendDAO();
    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        long currentUserId = (long) session.getAttribute("userId");
        String type = req.getParameter("type");

        if ("friends".equals(type)) {
            // Return JSON list of friends
            List<User> friends = friendDAO.getFriendsByUserId(currentUserId);
            List<Map<String, Object>> result = new ArrayList<>();
            for (User f : friends) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", f.getId());
                map.put("username", f.getUsername());
                map.put("fullName", f.getFullName());
                map.put("avatar", f.getAvatar());
                result.add(map);
            }
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(gson.toJson(result));
            return;
        }

        if ("requests".equals(type)) {
            // Return JSON list of pending requests
            List<FriendRequest> requests = friendDAO.getPendingRequests(currentUserId);
            List<Map<String, Object>> result = new ArrayList<>();
            for (FriendRequest req2 : requests) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", req2.getId());
                map.put("senderUsername", req2.getSender().getUsername());
                map.put("senderFullName", req2.getSender().getFullName());
                map.put("status", req2.getStatus());
                map.put("createdAt", req2.getCreatedAt());
                result.add(map);
            }
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(gson.toJson(result));
            return;
        }

        // Default: show friend.jsp page
        List<User> friends = friendDAO.getFriendsByUserId(currentUserId);
        List<FriendRequest> requests = friendDAO.getPendingRequests(currentUserId);
        req.setAttribute("friends", friends);
        req.setAttribute("friendRequests", requests);
        req.getRequestDispatcher("/WEB-INF/views/friend.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        long currentUserId = (long) session.getAttribute("userId");
        String action = req.getParameter("action");

        if (action == null)
            action = "";

        switch (action) {
            case "request": {
                String receiverUsername = req.getParameter("receiverUsername");
                User receiver = userDAO.findByUsername(receiverUsername);
                if (receiver != null) {
                    friendDAO.sendRequest(currentUserId, receiver.getId());
                }
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"status\":\"ok\"}");
                break;
            }
            case "accept": {
                long requestId = Long.parseLong(req.getParameter("requestId"));
                FriendRequest fr = friendDAO.findRequestById(requestId);
                if (fr != null) {
                    friendDAO.updateRequestStatus(requestId, "ACCEPTED");
                    friendDAO.addFriendship(fr.getSender().getId(), fr.getReceiver().getId());
                    friendDAO.addFriendship(fr.getReceiver().getId(), fr.getSender().getId());
                }
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"status\":\"ok\"}");
                break;
            }
            case "reject": {
                long requestId = Long.parseLong(req.getParameter("requestId"));
                friendDAO.updateRequestStatus(requestId, "REJECTED");
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"status\":\"ok\"}");
                break;
            }
            default:
                resp.sendError(400, "Unknown action");
        }
    }
}
