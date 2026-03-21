package com.ptit.socialchat.servlet;

import com.ptit.socialchat.dao.PostDAO;
import com.ptit.socialchat.dao.UserDAO;
import com.ptit.socialchat.model.Post;
import com.ptit.socialchat.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final PostDAO postDAO = new PostDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        long currentUserId = (long) session.getAttribute("userId");
        User user = userDAO.findById(currentUserId);

        List<Post> allPosts = postDAO.findAllOrderByCreatedAtDesc();
        List<Post> userPosts = allPosts.stream()
                .filter(p -> p.getUser().getId() == currentUserId)
                .collect(Collectors.toList());
        for (Post p : userPosts) {
            p.setLikeCount(postDAO.getLikeCount(p.getId()));
            p.setComments(postDAO.findCommentsByPostId(p.getId()));
        }

        req.setAttribute("profileUser", user);
        req.setAttribute("userPosts", userPosts);
        req.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        long currentUserId = (long) session.getAttribute("userId");

        String fullName = req.getParameter("fullName");
        String avatar = req.getParameter("avatar");

        User user = userDAO.findById(currentUserId);
        if (user == null) {
            resp.sendError(404, "User not found");
            return;
        }

        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName.trim());
        }
        if (avatar != null && !avatar.trim().isEmpty()) {
            user.setAvatar(avatar.trim());
        }
        userDAO.update(user);

        // Update session
        session.setAttribute("fullName", user.getFullName());
        session.setAttribute("avatar", user.getAvatar());

        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("{\"status\":\"ok\",\"fullName\":\"" + user.getFullName() + "\",\"avatar\":\"" +
                (user.getAvatar() != null ? user.getAvatar() : "") + "\"}");
    }
}
