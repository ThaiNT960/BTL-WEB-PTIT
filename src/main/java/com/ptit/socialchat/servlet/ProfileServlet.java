package com.ptit.socialchat.servlet;

import com.google.gson.Gson;
import com.ptit.socialchat.dao.PostDAO;
import com.ptit.socialchat.dao.UserDAO;
import com.ptit.socialchat.dao.FriendDAO;
import com.ptit.socialchat.model.FriendRequest;
import com.ptit.socialchat.model.Post;
import com.ptit.socialchat.model.User;
import com.ptit.socialchat.service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.ptit.socialchat.util.FileUtil;

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class ProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final PostDAO postDAO = new PostDAO();
    private final FriendDAO friendDAO = new FriendDAO();
    private final AuthService authService = new AuthService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        long currentUserId = (long) session.getAttribute("userId");
        
        String targetUsername = req.getParameter("username");
        User user = null;
        if (targetUsername != null && !targetUsername.trim().isEmpty()) {
            user = userDAO.findByUsername(targetUsername.trim());
            if (user == null) {
                // Return 404 if invalid user requested
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Người dùng không tồn tại.");
                return;
            }
        } else {
            user = userDAO.findById(currentUserId);
        }

        final User profileUser = user;

        List<Post> allPosts = postDAO.findAllOrderByCreatedAtDesc();
        List<Post> userPosts = allPosts.stream()
                .filter(p -> p.getUser().getId() == profileUser.getId())
                .collect(Collectors.toList());
        for (Post p : userPosts) {
            p.setLikeCount(postDAO.getLikeCount(p.getId()));
            p.setComments(postDAO.findCommentsByPostId(p.getId()));
        }

        req.setAttribute("profileUser", profileUser);
        req.setAttribute("userPosts", userPosts);

        // Compute relationship if viewing someone else's profile
        if (profileUser.getId() != currentUserId) {
            String relationshipStatus = "NONE";
            long friendRequestId = 0;
            if (friendDAO.isFriend(currentUserId, profileUser.getId())) {
                relationshipStatus = "FRIENDS";
            } else if (friendDAO.hasPendingRequestFromTo(currentUserId, profileUser.getId())) {
                relationshipStatus = "PENDING_SENT";
            } else {
                List<FriendRequest> received = friendDAO.getPendingRequests(currentUserId);
                java.util.Optional<FriendRequest> inbound = received.stream()
                        .filter(r -> r.getSender().getId() == profileUser.getId())
                        .findFirst();
                if (inbound.isPresent()) {
                    relationshipStatus = "PENDING_RECEIVED";
                    friendRequestId = inbound.get().getId();
                }
            }
            req.setAttribute("relationshipStatus", relationshipStatus);
            req.setAttribute("friendRequestId", friendRequestId);
        }

        req.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        long currentUserId = (long) session.getAttribute("userId");

        String action = req.getParameter("action");
        if ("change_password".equals(action)) {
            String oldPassword = req.getParameter("oldPassword");
            String newPassword = req.getParameter("newPassword");
            try {
                authService.changePassword(currentUserId, oldPassword, newPassword);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write(gson.toJson(java.util.Collections.singletonMap("status", "ok")));
            } catch (IllegalArgumentException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write(gson.toJson(Collections.singletonMap("error", e.getMessage())));
            }
            return;
        }

        String fullName = req.getParameter("fullName");
        String avatar = req.getParameter("avatar");

        User user = userDAO.findById(currentUserId);
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(gson.toJson(java.util.Collections.singletonMap("error", "User not found")));
            return;
        }

        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName.trim());
        }

        // Handle avatar update (multipart or URL)
        String contentType = req.getContentType();
        if (contentType != null && contentType.toLowerCase().contains("multipart/form-data")) {
            try {
                Part avatarPart = req.getPart("avatarFile");
                if (avatarPart != null && avatarPart.getSize() > 0) {
                    String avatarUrl = FileUtil.saveUploadedFile(req, avatarPart, "avatars");
                    user.setAvatar(avatarUrl);
                } else if (avatar != null && !avatar.trim().isEmpty()) {
                    user.setAvatar(avatar.trim());
                }
            } catch (Exception e) {
                // Potential issue with getPart in some containers if not carefully configured
                if (avatar != null && !avatar.trim().isEmpty()) {
                    user.setAvatar(avatar.trim());
                }
            }
        } else if (avatar != null && !avatar.trim().isEmpty()) {
            user.setAvatar(avatar.trim());
        }

        userDAO.update(user);

        // Update session
        session.setAttribute("fullName", user.getFullName());
        session.setAttribute("avatar", user.getAvatar());

        resp.setContentType("application/json;charset=UTF-8");
        java.util.Map<String, String> res = new java.util.HashMap<>();
        res.put("status", "ok");
        res.put("fullName", user.getFullName());
        res.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
        resp.getWriter().write(gson.toJson(res));
    }
}
