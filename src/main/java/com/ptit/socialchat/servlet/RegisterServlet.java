package com.ptit.socialchat.servlet;

import com.ptit.socialchat.dao.UserDAO;
import com.ptit.socialchat.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;

public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String fullName = req.getParameter("fullName");
        String password = req.getParameter("password");

        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            req.setAttribute("error", "Vui lòng điền đầy đủ thông tin");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
            return;
        }

        if (userDAO.findByUsername(username.trim()) != null) {
            req.setAttribute("error", "Tên đăng nhập đã tồn tại");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
            return;
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(password);
        user.setFullName(fullName != null ? fullName.trim() : username.trim());
        user.setRole("ROLE_USER");
        userDAO.save(user);

        resp.sendRedirect(req.getContextPath() + "/LoginServlet?registered=true");
    }
}
