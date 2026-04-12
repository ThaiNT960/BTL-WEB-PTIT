package com.ptit.socialchat.servlet;

import com.ptit.socialchat.util.FileUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/UploadChatImage")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 10, maxRequestSize = 1024 * 1024 * 10 * 5)
public class UploadChatImageServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(new com.google.gson.Gson().toJson(java.util.Collections.singletonMap("error", "Unauthorized")));
            return;
        }

        try {
            Part imagePart = req.getPart("imageFile");
            if (imagePart != null && imagePart.getSize() > 0) {
                String imageUrl = FileUtil.saveUploadedFile(req, imagePart, "chats");
                resp.setContentType("application/json;charset=UTF-8");
                java.util.Map<String, String> res = new java.util.HashMap<>();
                res.put("status", "ok");
                res.put("imageUrl", imageUrl);
                resp.getWriter().write(new com.google.gson.Gson().toJson(res));
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(new com.google.gson.Gson().toJson(java.util.Collections.singletonMap("error", "No image file provided")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(new com.google.gson.Gson().toJson(java.util.Collections.singletonMap("error", "Server error during upload")));
        }
    }
}
