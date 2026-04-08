package com.ptit.socialchat.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileUtil {

    /**
     * Saves the uploaded Part to both the Tomcat deployment directory (for immediate serving)
     * and the project's src directory (for persistence).
     *
     * @param request       The HttpServletRequest
     * @param part          The uploaded file part
     * @param subDirectory  The sub-directory inside uploads (e.g. "avatars", "chats")
     * @return The URL path to access the file (e.g. "/socialchat/uploads/avatars/filename.jpg")
     */
    public static String saveUploadedFile(HttpServletRequest request, Part part, String subDirectory) throws IOException {
        String fileName = UUID.randomUUID().toString() + "-" + extractFileName(part);
        
        // 1. Get Tomcat's deployment directory (for immediate serving)
        String deployUploadPath = request.getServletContext().getRealPath("/") + "uploads" + File.separator + subDirectory;
        File deployUploadDir = new File(deployUploadPath);
        if (!deployUploadDir.exists()) {
            deployUploadDir.mkdirs();
        }
        
        // 2. Try to persist in src directory (for development)
        // Check if we are running in dev mode (user.dir contains /src/)
        String userDir = System.getProperty("user.dir");
        String srcUploadPath = null;
        
        // Try multiple strategies to find project root/src
        if (userDir.contains("BTL-WEB-PTIT")) {
            srcUploadPath = userDir + File.separator + "src" + File.separator + "main" + File.separator + "webapp" + File.separator + "uploads" + File.separator + subDirectory;
        } else {
            // Fallback: search for pom.xml parent up to 3 levels
            File current = new File(userDir);
            for (int i = 0; i < 3; i++) {
                if (new File(current, "pom.xml").exists()) {
                    srcUploadPath = current.getAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator + "webapp" + File.separator + "uploads" + File.separator + subDirectory;
                    break;
                }
                current = current.getParentFile();
                if (current == null) break;
            }
        }

        try (InputStream input = part.getInputStream()) {
            byte[] bytes = input.readAllBytes();
            
            // Always write to deployment directory
            Path deployPath = Paths.get(deployUploadPath, fileName);
            Files.write(deployPath, bytes);
            
            // If src path exists, write there too
            if (srcUploadPath != null) {
                File srcUploadDir = new File(srcUploadPath);
                if (!srcUploadDir.exists()) {
                    srcUploadDir.mkdirs();
                }
                Path srcPath = Paths.get(srcUploadPath, fileName);
                Files.write(srcPath, bytes);
            }
        } catch (Exception e) {
            System.err.println("Error saving file to persistence: " + e.getMessage());
            // We already wrote to deployPath if possible, or it will throw IOException
        }

        // Return the context-relative URL
        return request.getContextPath() + "/uploads/" + subDirectory + "/" + fileName;
    }

    private static String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                return s.substring(s.indexOf("=") + 2, s.length() - 1);
            }
        }
        return "unknown";
    }
}
