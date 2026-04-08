package com.ptit.socialchat.servlet;

import com.google.gson.Gson;
import com.ptit.socialchat.dao.PostDAO;
import com.ptit.socialchat.model.Comment;
import com.ptit.socialchat.model.Post;
import com.ptit.socialchat.service.PostService;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

public class PostServlet extends HttpServlet {

    private final PostDAO postDAO = new PostDAO();
    private final PostService postService = new PostService();
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
            map.put("createdAt", post.getCreatedAt() != null ? post.getCreatedAt().toString() : "");
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
                String cCreatedAt = c.getCreatedAt() != null ? c.getCreatedAt().toString() : "";
                cm.put("createdAt", cCreatedAt);
                cm.put("username", c.getUser().getUsername());
                cm.put("userFullName", c.getUser().getFullName());
                cm.put("avatar", c.getUser().getAvatar());
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
        System.out.println("[PostServlet] handleCreatePost called: userId=" + userId + ", content=" + content + ", imageUrl=" + imageUrl);
        try {
            postService.createPost(userId, content, imageUrl);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"status\":\"ok\"}");
            System.out.println("[PostServlet] Post created successfully");
        } catch (IllegalArgumentException e) {
            System.err.println("[PostServlet] Validation error: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            System.err.println("[PostServlet] Unexpected error creating post: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"error\": \"Lỗi hệ thống khi đăng bài: " + e.getMessage() + "\"}");
        }
    }

    private void handleAddComment(HttpServletRequest req, HttpServletResponse resp, long userId)
            throws IOException {
        long postId = Long.parseLong(req.getParameter("postId"));
        String content = req.getParameter("content");
        try {
            postService.addComment(userId, postId, content);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"status\":\"ok\"}");
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private void handleToggleLike(HttpServletRequest req, HttpServletResponse resp, long userId)
            throws IOException {
        long postId = Long.parseLong(req.getParameter("postId"));
        try {
            boolean liked = postService.toggleLike(userId, postId);
            int likeCount = postDAO.getLikeCount(postId);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"liked\":" + liked + ",\"likeCount\":" + likeCount + "}");
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private void handleDeletePost(HttpServletRequest req, HttpServletResponse resp,
            long currentUserId, HttpSession session) throws IOException {
        long postId = Long.parseLong(req.getParameter("postId"));
        String role = (String) session.getAttribute("role");
        try {
            postService.deletePost(currentUserId, postId, role);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"status\":\"ok\"}");
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
