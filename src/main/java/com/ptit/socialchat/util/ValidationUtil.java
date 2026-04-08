package com.ptit.socialchat.util;

public class ValidationUtil {

    // Username chỉ chứa chữ cái, số, dấu gạch dưới, dài 8-50 ký tự
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]{8,50}$";

    public static boolean isValidUsername(String username) {
        if (username == null) return false;
        return username.matches(USERNAME_PATTERN);
    }

    public static boolean isValidPassword(String password) {
        // Tối thiểu 6 ký tự
        return password != null && password.length() >= 6 && password.length() <= 100;
    }

    public static boolean isValidFullName(String fullName) {
        // Tên không rỗng, tối đa 100 ký tự
        return fullName != null && !fullName.trim().isEmpty() && fullName.length() <= 100;
    }

    public static boolean isValidMessageContent(String content) {
        // Tin nhắn không rỗng và không quá 2000 ký tự
        return content != null && !content.trim().isEmpty() && content.trim().length() <= 2000;
    }

    public static boolean isValidPostContent(String content) {
        return content != null && !content.trim().isEmpty() && content.trim().length() <= 5000;
    }

    public static boolean isValidCommentContent(String content) {
        return content != null && !content.trim().isEmpty() && content.trim().length() <= 500;
    }
}
