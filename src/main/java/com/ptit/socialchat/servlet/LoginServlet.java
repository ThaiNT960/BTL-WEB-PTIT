package com.ptit.socialchat.servlet;

import com.ptit.socialchat.model.User;
import com.ptit.socialchat.service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;

public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // If already logged in, redirect to home
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            if ("ROLE_ADMIN".equals(session.getAttribute("role"))) {
                resp.sendRedirect(req.getContextPath() + "/AdminServlet");
            } else {
                resp.sendRedirect(req.getContextPath() + "/HomeServlet");
            }
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        try {
            User user = authService.login(username, password);
            HttpSession session = req.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("fullName", user.getFullName());
            session.setAttribute("avatar", user.getAvatar());
            session.setAttribute("role", user.getRole());
            if ("ROLE_ADMIN".equals(user.getRole())) {
                resp.sendRedirect(req.getContextPath() + "/AdminServlet");
            } else {
                resp.sendRedirect(req.getContextPath() + "/HomeServlet");
            }
        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        }
    }
}
