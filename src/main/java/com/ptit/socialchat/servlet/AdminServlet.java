package com.ptit.socialchat.servlet;

import com.google.gson.Gson;
import com.ptit.socialchat.dao.PostDAO;
import com.ptit.socialchat.dao.UserDAO;
import com.ptit.socialchat.model.Post;
import com.ptit.socialchat.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

public class AdminServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final PostDAO postDAO = new PostDAO();
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

        // Default: show admin.jsp
        List<User> users = userDAO.findAll();
        List<Post> posts = postDAO.findAllOrderByCreatedAtDesc();
        req.setAttribute("users", users);
        req.setAttribute("posts", posts);
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
                resp.getWriter().write("{\"status\":\"ok\"}");
                break;
            }
            case "deletePost": {
                long postId = Long.parseLong(req.getParameter("postId"));
                postDAO.delete(postId);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"status\":\"ok\"}");
                break;
            }
            case "createUser": {
                String username = req.getParameter("username");
                String fullName = req.getParameter("fullName");
                String password = req.getParameter("password");
                if (userDAO.findByUsername(username) == null) {
                    User u = new User();
                    u.setUsername(username);
                    u.setFullName(fullName);
                    u.setPassword(password);
                    u.setRole("ROLE_USER");
                    userDAO.save(u);
                }
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"status\":\"ok\"}");
                break;
            }
            default:
                resp.sendError(400, "Unknown action");
        }
    }
}
