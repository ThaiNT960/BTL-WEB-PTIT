package com.ptit.socialchat.servlet;

import com.google.gson.Gson;
import com.ptit.socialchat.dao.PostDAO;
import com.ptit.socialchat.model.Comment;
import com.ptit.socialchat.model.Post;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

public class PostServlet extends HttpServlet {

    private final PostDAO postDAO = new PostDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Return JSON list of posts for AJAX
        HttpSession session = req.getSession(false);
        long currentUserId = (long) session.getAttribute("userId");

        List<Post> posts = postDAO.findAllOrderByCreatedAtDesc();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Post post : posts) {
            List<Comment> comments = postDAO.findCommentsByPostId(post.getId());
            post.setComments(comments);
            post.setLikeCount(postDAO.getLikeCount(post.getId()));
            post.setLiked(postDAO.isLikedByUser(post.getId(), currentUserId));

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", post.getId());
            map.put("content", post.getContent());
            map.put("imageUrl", post.getImageUrl());
            map.put("createdAt", post.getCreatedAt());
            map.put("username", post.getUser().getUsername());
            map.put("userFullName", post.getUser().getFullName());
            map.put("avatar", post.getUser().getAvatar());
            map.put("likeCount", post.getLikeCount());
            map.put("liked", post.isLiked());

            List<Map<String, Object>> commentList = new ArrayList<>();
            for (Comment c : comments) {
                Map<String, Object> cm = new LinkedHashMap<>();
                cm.put("id", c.getId());
                cm.put("content", c.getContent());
                cm.put("createdAt", c.getCreatedAt());
                cm.put("username", c.getUser().getUsername());
                cm.put("userFullName", c.getUser().getFullName());
                commentList.add(cm);
            }
            map.put("comments", commentList);
            result.add(map);
        }

        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(gson.toJson(result));
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
            case "create":
                handleCreatePost(req, resp, currentUserId);
                break;
            case "comment":
                handleAddComment(req, resp, currentUserId);
                break;
            case "like":
                handleToggleLike(req, resp, currentUserId);
                break;
            case "delete":
                handleDeletePost(req, resp, currentUserId, session);
                break;
            default:
                resp.sendError(400, "Unknown action");
        }
    }

    private void handleCreatePost(HttpServletRequest req, HttpServletResponse resp, long userId)
            throws IOException {
        String content = req.getParameter("content");
        String imageUrl = req.getParameter("imageUrl");
        if (content == null || content.trim().isEmpty()) {
            resp.sendError(400, "Content cannot be empty");
            return;
        }
        postDAO.save(content.trim(), (imageUrl != null && !imageUrl.trim().isEmpty()) ? imageUrl.trim() : null, userId);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("{\"status\":\"ok\"}");
    }

    private void handleAddComment(HttpServletRequest req, HttpServletResponse resp, long userId)
            throws IOException {
        long postId = Long.parseLong(req.getParameter("postId"));
        String content = req.getParameter("content");
        if (content == null || content.trim().isEmpty()) {
            resp.sendError(400, "Content cannot be empty");
            return;
        }
        postDAO.addComment(postId, content.trim(), userId);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("{\"status\":\"ok\"}");
    }

    private void handleToggleLike(HttpServletRequest req, HttpServletResponse resp, long userId)
            throws IOException {
        long postId = Long.parseLong(req.getParameter("postId"));
        boolean liked = postDAO.toggleLike(postId, userId);
        int likeCount = postDAO.getLikeCount(postId);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("{\"liked\":" + liked + ",\"likeCount\":" + likeCount + "}");
    }

    private void handleDeletePost(HttpServletRequest req, HttpServletResponse resp,
            long currentUserId, HttpSession session) throws IOException {
        long postId = Long.parseLong(req.getParameter("postId"));
        String role = (String) session.getAttribute("role");
        Post post = postDAO.findById(postId);
        if (post == null) {
            resp.sendError(404, "Post not found");
            return;
        }
        if (post.getUser().getId() != currentUserId && !"ROLE_ADMIN".equals(role)) {
            resp.sendError(403, "Not authorized");
            return;
        }
        postDAO.delete(postId);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("{\"status\":\"ok\"}");
    }
}
