package com.ptit.socialchat.servlet;

import com.google.gson.Gson;
import com.ptit.socialchat.dao.UserDAO;
import com.ptit.socialchat.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

public class SearchServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String currentUsername = (String) session.getAttribute("username");
        String keyword = req.getParameter("keyword");

        if (keyword == null || keyword.trim().isEmpty()) {
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("[]");
            return;
        }

        List<User> users = userDAO.searchByKeyword(keyword.trim());
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : users) {
            if (u.getUsername().equals(currentUsername))
                continue;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("fullName", u.getFullName());
            map.put("avatar", u.getAvatar());
            result.add(map);
        }

        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(gson.toJson(result));
    }
}
