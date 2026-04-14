package com.ptit.socialchat.servlet;

import com.google.gson.Gson;
import com.ptit.socialchat.dao.AnnouncementDAO;
import com.ptit.socialchat.dao.ModerationSettingsDAO;
import com.ptit.socialchat.dao.PostDAO;
import com.ptit.socialchat.dao.UserDAO;
import com.ptit.socialchat.model.Announcement;
import com.ptit.socialchat.model.ModerationSettings;
import com.ptit.socialchat.model.Post;
import com.ptit.socialchat.model.User;
import com.ptit.socialchat.service.ModerationService;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

public class AdminServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final PostDAO postDAO = new PostDAO();
    private final ModerationSettingsDAO moderationSettingsDAO = new ModerationSettingsDAO();
    private final ModerationService moderationService = new ModerationService();
    private final AnnouncementDAO announcementDAO = new AnnouncementDAO();
    private final Gson gson = new Gson();

    private boolean isAdmin(HttpSession session) {
        return "ROLE_ADMIN".equals(session.getAttribute("role"));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (!isAdmin(session)) {
            resp.sendRedirect(req.getContextPath() + "/HomeServlet");
            return;
        }

        String type = req.getParameter("type");
        if ("users".equals(type)) {
            // Return JSON list of users for admin AJAX
            List<User> users = userDAO.findAll();
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(gson.toJson(users));
            return;
        }
        if ("checkAiService".equals(type)) {
            // Kiểm tra AI service qua GET (không cần CSRF)
            ModerationSettings settings = moderationSettingsDAO.getSettings();
            boolean available = moderationService.isServiceAvailable(settings.getAiServiceUrl());
            resp.setContentType("application/json;charset=UTF-8");
            java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("available", available);
            result.put("url", settings.getAiServiceUrl());
            resp.getWriter().write(gson.toJson(result));
            System.out.println("[AdminServlet] AI Service check: " + (available ? "AVAILABLE" : "UNAVAILABLE") + " at " + settings.getAiServiceUrl());
            return;
        }

        // Default: show admin.jsp
        List<User> users = userDAO.findAll();
        List<Post> posts = postDAO.findAllForAdmin();
        List<Post> pendingPosts = postDAO.findByStatus("PENDING");
        List<Post> rejectedPosts = postDAO.findByStatus("REJECTED");
        ModerationSettings settings = moderationSettingsDAO.getSettings();

        // Kiểm tra AI service có đang chạy không
        boolean aiServiceAvailable = moderationService.isServiceAvailable(settings.getAiServiceUrl());

        req.setAttribute("users", users);
        req.setAttribute("posts", posts);
        req.setAttribute("pendingPosts", pendingPosts);
        req.setAttribute("rejectedPosts", rejectedPosts);
        req.setAttribute("moderationMode", settings.getMode());
        req.setAttribute("aiServiceUrl", settings.getAiServiceUrl());
        req.setAttribute("aiServiceAvailable", aiServiceAvailable);
        req.setAttribute("pendingCount", postDAO.countByStatus("PENDING"));
        req.setAttribute("rejectedCount", postDAO.countByStatus("REJECTED"));

        // Thông báo từ admin
        List<Announcement> announcements = announcementDAO.findAll();
        req.setAttribute("announcements", announcements);

        // Truyền userId của admin đang đăng nhập (để ẩn nút xóa chính mình)
        req.setAttribute("currentAdminId", session.getAttribute("userId"));

        req.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (!isAdmin(session)) {
            resp.sendError(403, "Forbidden");
            return;
        }

        String action = req.getParameter("action");
        if (action == null)
            action = "";

        switch (action) {
            case "deleteUser": {
                long userId = Long.parseLong(req.getParameter("userId"));
                userDAO.delete(userId);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write(gson.toJson(java.util.Collections.singletonMap("status", "ok")));
                break;
            }
            case "deletePost": {
                long postId = Long.parseLong(req.getParameter("postId"));
                postDAO.delete(postId);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write(gson.toJson(java.util.Collections.singletonMap("status", "ok")));
                break;
            }
            case "createUser": {
                String username = req.getParameter("username");
                String fullName = req.getParameter("fullName");
                String password = req.getParameter("password");
                resp.setContentType("application/json;charset=UTF-8");

                try {
                    if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty() || password.length() < 6) {
                        resp.setStatus(400);
                        resp.getWriter().write(gson.toJson(java.util.Collections.singletonMap("error", "Tên đăng nhập hoặc mật khẩu không hợp lệ.")));
                        return;
                    }

                    if (userDAO.findByUsername(username.trim()) == null) {
                        User u = new User();
                        u.setUsername(username.trim());
                        u.setFullName(fullName != null ? fullName.trim() : username.trim());
                        u.setPassword(org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt(12)));
                        u.setRole("ROLE_USER");
                        userDAO.save(u);
                        resp.getWriter().write(gson.toJson(java.util.Collections.singletonMap("status", "ok")));
                    } else {
                        resp.setStatus(400);
                        resp.getWriter().write(gson.toJson(java.util.Collections.singletonMap("error", "Tên đăng nhập đã tồn tại.")));
                    }
                } catch (Exception e) {
                    resp.setStatus(500);
                    resp.getWriter().write(gson.toJson(java.util.Collections.singletonMap("error", "Lỗi server nội bộ.")));
                }
                break;
            }

            // ═══════════════════════════════════════════════════
            //  MODERATION ACTIONS
            // ═══════════════════════════════════════════════════

            case "setModerationMode": {
                String mode = req.getParameter("mode");
                resp.setContentType("application/json;charset=UTF-8");
                if (mode != null && ("NONE".equals(mode) || "MANUAL".equals(mode) || "AUTO_AI".equals(mode))) {
                    moderationSettingsDAO.updateMode(mode);
                    java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
                    result.put("status", "ok");
                    result.put("mode", mode);
                    resp.getWriter().write(gson.toJson(result));
                    System.out.println("[AdminServlet] Moderation mode changed to: " + mode);
                } else {
                    resp.setStatus(400);
                    resp.getWriter().write(gson.toJson(java.util.Collections.singletonMap("error", "Chế độ không hợp lệ.")));
                }
                break;
            }

            case "approvePost": {
                long postId = Long.parseLong(req.getParameter("postId"));
                postDAO.updatePostStatus(postId, "APPROVED");
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write(gson.toJson(java.util.Collections.singletonMap("status", "ok")));
                System.out.println("[AdminServlet] Post approved: " + postId);
                break;
            }

            case "rejectPost": {
                long postId = Long.parseLong(req.getParameter("postId"));
                postDAO.updatePostStatus(postId, "REJECTED");
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write(gson.toJson(java.util.Collections.singletonMap("status", "ok")));
                System.out.println("[AdminServlet] Post rejected: " + postId);
                break;
            }

            case "approveAll": {
                List<Post> pending = postDAO.findByStatus("PENDING");
                for (Post p : pending) {
                    postDAO.updatePostStatus(p.getId(), "APPROVED");
                }
                resp.setContentType("application/json;charset=UTF-8");
                java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
                result.put("status", "ok");
                result.put("count", pending.size());
                resp.getWriter().write(gson.toJson(result));
                System.out.println("[AdminServlet] Approved all: " + pending.size() + " posts");
                break;
            }

            case "checkAiService": {
                ModerationSettings settings = moderationSettingsDAO.getSettings();
                boolean available = moderationService.isServiceAvailable(settings.getAiServiceUrl());
                resp.setContentType("application/json;charset=UTF-8");
                java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
                result.put("available", available);
                result.put("url", settings.getAiServiceUrl());
                resp.getWriter().write(gson.toJson(result));
                break;
            }

            case "createAnnouncement": {
                String title = req.getParameter("title");
                String content = req.getParameter("content");
                long adminId = (long) session.getAttribute("userId");
                resp.setContentType("application/json;charset=UTF-8");
                if (title == null || title.trim().isEmpty()) {
                    resp.setStatus(400);
                    resp.getWriter().write(gson.toJson(java.util.Collections.singletonMap("error", "Tiêu đề không được để trống.")));
                } else {
                    announcementDAO.save(title.trim(), content != null ? content.trim() : "", adminId);
                    resp.getWriter().write(gson.toJson(java.util.Collections.singletonMap("status", "ok")));
                }
                break;
            }

            case "deleteAnnouncement": {
                long annId = Long.parseLong(req.getParameter("announcementId"));
                announcementDAO.delete(annId);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write(gson.toJson(java.util.Collections.singletonMap("status", "ok")));
                break;
            }

            default:
                resp.setStatus(400);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write(gson.toJson(java.util.Collections.singletonMap("error", "Unknown action")));
        }
    }
}
