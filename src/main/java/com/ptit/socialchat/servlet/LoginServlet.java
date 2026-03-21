package com.ptit.socialchat.servlet;

import com.ptit.socialchat.dao.UserDAO;
import com.ptit.socialchat.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;

public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // If already logged in, redirect to home
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            resp.sendRedirect(req.getContextPath() + "/HomeServlet");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        User user = userDAO.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            HttpSession session = req.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("fullName", user.getFullName());
            session.setAttribute("avatar", user.getAvatar());
            session.setAttribute("role", user.getRole());
            resp.sendRedirect(req.getContextPath() + "/HomeServlet");
        } else {
            req.setAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        }
    }
}
