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

public class SearchServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final FriendDAO friendDAO = new FriendDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String currentUsername = (String) session.getAttribute("username");
        String keyword = req.getParameter("keyword");

        if (keyword == null || keyword.trim().isEmpty()) {
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("[]");
            return;
        }

        long currentUserId = (long) session.getAttribute("userId");
        List<User> users = userDAO.searchByKeyword(keyword.trim());
        
        // Fetch current user's pending requests ONCE
        List<FriendRequest> myPendingRequests = friendDAO.getPendingRequests(currentUserId);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : users) {
            if (u.getUsername().equals(currentUsername))
                continue;

            String status = "NONE";
            if (friendDAO.isFriend(currentUserId, u.getId())) {
                status = "FRIENDS";
            } else {
                // Check if I sent a request to U
                // (This is still one call per user, but better than nested loop or fetching all requests for U)
                if (friendDAO.hasPendingRequestFromTo(currentUserId, u.getId())) {
                    status = "PENDING_SENT";
                } else {
                    // Check if U sent a request to me (from our cached list)
                    boolean receivedFromU = myPendingRequests.stream()
                        .anyMatch(r -> r.getSender().getId() == u.getId());
                    if (receivedFromU) {
                        status = "PENDING_RECEIVED";
                    }
                }
            }

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("fullName", u.getFullName());
            map.put("avatar", u.getAvatar());
            map.put("relationshipStatus", status);
            result.add(map);
        }

        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(gson.toJson(result));
    }
}
