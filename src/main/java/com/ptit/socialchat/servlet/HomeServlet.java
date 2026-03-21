package com.ptit.socialchat.servlet;

import com.ptit.socialchat.dao.PostDAO;
import com.ptit.socialchat.model.Comment;
import com.ptit.socialchat.model.Post;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

public class HomeServlet extends HttpServlet {

    private final PostDAO postDAO = new PostDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        long currentUserId = (long) session.getAttribute("userId");

        List<Post> posts = postDAO.findAllOrderByCreatedAtDesc();
        for (Post post : posts) {
            List<Comment> comments = postDAO.findCommentsByPostId(post.getId());
            post.setComments(comments);
            post.setLikeCount(postDAO.getLikeCount(post.getId()));
            post.setLiked(postDAO.isLikedByUser(post.getId(), currentUserId));
        }
        req.setAttribute("posts", posts);
        req.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(req, resp);
    }
}
