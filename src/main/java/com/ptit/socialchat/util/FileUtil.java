package com.ptit.socialchat.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileUtil {

    public static String saveUploadedFile(HttpServletRequest request, Part part, String subDirectory) throws IOException {
        String originalFileName = extractFileName(part);
        String ext = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            ext = originalFileName.substring(dotIndex + 1).toLowerCase();
        }
        if (!ext.matches("^(jpg|jpeg|png|gif|webp)$")) {
            throw new IllegalArgumentException("Định dạng tệp không hợp lệ. Chỉ chấp nhận jpg, jpeg, png, gif, webp.");
        }
        String fileName = UUID.randomUUID().toString() + "-" + originalFileName;
        
        // 1. Lưu vào thư mục triển khai của Tomcat
        String deployUploadPath = request.getServletContext().getRealPath("/") + "uploads" + File.separator + subDirectory;
        File deployUploadDir = new File(deployUploadPath);
        if (!deployUploadDir.exists()) {
            deployUploadDir.mkdirs();
        }
        
        // 2. Lưu đồng thời vào thư mục nguồn src/ (nếu tìm thấy thư mục dự án)
        String srcUploadPath = null;
        File projectRoot = findProjectRoot(request);
        if (projectRoot != null) {
            srcUploadPath = projectRoot.getAbsolutePath() + File.separator + "src" 
                + File.separator + "main" + File.separator + "webapp" 
                + File.separator + "uploads" + File.separator + subDirectory;
        }

        try (InputStream input = part.getInputStream()) {
            byte[] bytes = input.readAllBytes();
            
            // Ghi file vào thư mục triển khai
            Path deployPath = Paths.get(deployUploadPath, fileName);
            Files.write(deployPath, bytes);
            
            // Ghi file vào thư mục nguồn
            if (srcUploadPath != null) {
                File srcUploadDir = new File(srcUploadPath);
                if (!srcUploadDir.exists()) {
                    srcUploadDir.mkdirs();
                }
                Path srcPath = Paths.get(srcUploadPath, fileName);
                Files.write(srcPath, bytes);
            }
        } catch (Exception e) {
            System.err.println("Lỗi lưu trữ tệp tin: " + e.getMessage());
        }

        return request.getContextPath() + "/uploads/" + subDirectory + "/" + fileName;
    }

    /**
     * Trích xuất tên tệp gốc từ header Content-Disposition của Multipart.
     */
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

    /**
     * Tự động dò tìm thư mục gốc chứa file pom.xml của dự án.
     * Tương thích chuẩn xác trên cả VS Code (qua user.dir) và IntelliJ IDEA (qua realPath).
     */
    private static File findProjectRoot(HttpServletRequest request) {
        // Cách 1: Dò từ realPath của Servlet (dành cho IntelliJ IDEA)
        try {
            String realPath = request.getServletContext().getRealPath("/");
            if (realPath != null) {
                File dir = new File(realPath);
                for (int i = 0; i < 5; i++) {
                    if (dir != null && new File(dir, "pom.xml").exists()) {
                        return dir;
                    }
                    if (dir != null) dir = dir.getParentFile();
                }
            }
        } catch (Exception ignored) {}

        // Cách 2: Dò từ user.dir (dành cho VS Code / Terminal)
        try {
            String userDir = System.getProperty("user.dir");
            if (userDir != null) {
                File dir = new File(userDir);
                for (int i = 0; i < 5; i++) {
                    if (dir != null && new File(dir, "pom.xml").exists()) {
                        return dir;
                    }
                    if (dir != null) dir = dir.getParentFile();
                }
            }
        } catch (Exception ignored) {}

        return null;
    }
}
