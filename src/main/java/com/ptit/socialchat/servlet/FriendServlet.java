package com.ptit.socialchat.servlet;

import com.google.gson.Gson;
import com.ptit.socialchat.dao.FriendDAO;
import com.ptit.socialchat.model.FriendRequest;
import com.ptit.socialchat.model.User;
import java.util.List;
import com.ptit.socialchat.service.FriendService;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

public class FriendServlet extends HttpServlet {

    private final FriendDAO friendDAO = new FriendDAO();
    private final FriendService friendService = new FriendService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        long currentUserId = (long) session.getAttribute("userId");
        String type = req.getParameter("type");

        if ("friends".equals(type)) {
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
            List<FriendRequest> requests = friendDAO.getPendingRequests(currentUserId);
            List<Map<String, Object>> result = new ArrayList<>();
            for (FriendRequest req2 : requests) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", req2.getId());
                map.put("senderUsername", req2.getSender().getUsername());
                map.put("senderFullName", req2.getSender().getFullName());
                map.put("status", req2.getStatus());
                map.put("createdAt", req2.getCreatedAt() != null ? req2.getCreatedAt().toString() : "");
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
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return;
        }

        long currentUserId = (long) session.getAttribute("userId");
        String action = req.getParameter("action");

        if (action == null) {

            action = "";
        }

        try {
            switch (action) {
                case "request": {
                    String receiverUsername = req.getParameter("receiverUsername");
                    try {
                        friendService.sendFriendRequest(currentUserId, receiverUsername);
                        resp.setContentType("application/json;charset=UTF-8");
                        resp.getWriter().write("{\"status\":\"ok\"}");
                    } catch (IllegalArgumentException e) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.setContentType("application/json;charset=UTF-8");
                        resp.getWriter().write(gson.toJson(Collections.singletonMap("error", e.getMessage())));
                    }
                    break;
                }
                case "accept": {
                    String reqIdStr = req.getParameter("requestId");
                    try {
                        long requestId = Long.parseLong(reqIdStr != null ? reqIdStr : "0");
                        friendService.acceptFriendRequest(requestId, currentUserId);
                        resp.setContentType("application/json;charset=UTF-8");
                        resp.getWriter().write("{\"status\":\"ok\"}");
                    } catch (Exception e) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.setContentType("application/json;charset=UTF-8");
                        resp.getWriter().write(gson.toJson(Collections.singletonMap("error", e.getMessage())));
                    }
                    break;
                }
                case "accept_by_username": {
                    String senderUsername = req.getParameter("senderUsername");
                    try {
                        friendService.acceptFriendRequestByUsername(currentUserId, senderUsername);
                        resp.setContentType("application/json;charset=UTF-8");
                        resp.getWriter().write("{\"status\":\"ok\"}");
                    } catch (Exception e) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.setContentType("application/json;charset=UTF-8");
                        resp.getWriter().write(gson.toJson(Collections.singletonMap("error", e.getMessage())));
                    }
                    break;
                }
                case "reject": {
                    String reqIdStr = req.getParameter("requestId");
                    try {
                        long requestId = Long.parseLong(reqIdStr != null ? reqIdStr : "0");
                        friendService.rejectFriendRequest(requestId, currentUserId);
                        resp.setContentType("application/json;charset=UTF-8");
                        resp.getWriter().write("{\"status\":\"ok\"}");
                    } catch (Exception e) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.setContentType("application/json;charset=UTF-8");
                        resp.getWriter().write(gson.toJson(Collections.singletonMap("error", e.getMessage())));
                    }
                    break;
                }
                case "unfriend": {
                    String targetUsername = req.getParameter("targetUsername");
                    try {
                        friendService.unfriend(currentUserId, targetUsername);
                        resp.setContentType("application/json;charset=UTF-8");
                        resp.getWriter().write("{\"status\":\"ok\"}");
                    } catch (Exception e) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.setContentType("application/json;charset=UTF-8");
                        resp.getWriter().write(gson.toJson(Collections.singletonMap("error", e.getMessage())));
                    }
                    break;
                }
                case "cancel": {
                    String targetUsername = req.getParameter("targetUsername");
                    try {
                        friendService.cancelRequest(currentUserId, targetUsername);
                        resp.setContentType("application/json;charset=UTF-8");
                        resp.getWriter().write("{\"status\":\"ok\"}");
                    } catch (Exception e) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.setContentType("application/json;charset=UTF-8");
                        resp.getWriter().write(gson.toJson(Collections.singletonMap("error", e.getMessage())));
                    }
                    break;
                }
                default:
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server Error");
        }
    }
}
