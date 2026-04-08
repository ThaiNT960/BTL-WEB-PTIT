package com.ptit.socialchat.service;

import com.ptit.socialchat.dao.UserDAO;
import com.ptit.socialchat.model.User;
import com.ptit.socialchat.util.ValidationUtil;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public User login(String username, String password) throws IllegalArgumentException {
        // Auto-seed admin user if missing
        try {
            if (userDAO.findByUsername("admin") == null) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setFullName("Administrator");
                admin.setPassword(BCrypt.hashpw("12345678", BCrypt.gensalt(12)));
                admin.setRole("ROLE_ADMIN");
                userDAO.save(admin);
                System.out.println("[INFO] Admin seeded.");
            }
        } catch (Exception ignored) {}

        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên đăng nhập và mật khẩu.");
        }

        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            throw new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không đúng.");
        }

        // Kiểm tra mật khẩu (Sử dụng BCrypt)
        // Lưu ý do dữ liệu cũ trong Database có thể là chuỗi Plain-Text (Chưa được hash),
        // BCrypt có thể ném Exception nên ta phải fallback check bằng tay tạm thời cho dữ liệu cũ (BTL)
        boolean passwordMatch = false;
        try {
            passwordMatch = BCrypt.checkpw(password, user.getPassword());
        } catch (Exception e) {
            // Mật khẩu hiện tại trên DB không mã hóa đúng chuẩn BCrypt
            if (password.equals(user.getPassword())) {
                passwordMatch = true;
                // Có thể update lại DB tại đây để chuyển sang Hash, nhưng ta bỏ qua để đơn giản
            }
        }

        if (!passwordMatch) {
            throw new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không đúng.");
        }

        return user;
    }

    public void register(String username, String password, String fullName) throws IllegalArgumentException {
        if (!ValidationUtil.isValidUsername(username)) {
            throw new IllegalArgumentException("Tên đăng nhập phải từ 8-50 ký tự, chỉ chứa chữ cái, số và dấu gạch dưới.");
        }

        if (!ValidationUtil.isValidPassword(password)) {
            throw new IllegalArgumentException("Mật khẩu phải chứa ít nhất 6 ký tự.");
        }

        String actualFullName = (fullName != null && !fullName.trim().isEmpty()) ? fullName.trim() : username.trim();
        if (!ValidationUtil.isValidFullName(actualFullName)) {
            throw new IllegalArgumentException("Họ và tên không hợp lệ quá 100 ký tự.");
        }

        if (userDAO.findByUsername(username.trim()) != null) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại trên hệ thống.");
        }

        // Băm mật khẩu (Hash) bằng chuẩn độ phức tạp (salt) = 12
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(hashedPassword);
        user.setFullName(actualFullName);
        user.setRole("ROLE_USER");

        userDAO.save(user);
    }

    public void changePassword(long userId, String oldPassword, String newPassword) throws IllegalArgumentException {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng.");
        }

        if (oldPassword == null || oldPassword.trim().isEmpty() || newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ mật khẩu cũ và mới.");
        }

        if (!ValidationUtil.isValidPassword(newPassword)) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }

        boolean passwordMatch = false;
        try {
            passwordMatch = BCrypt.checkpw(oldPassword, user.getPassword());
        } catch (Exception e) {
            if (oldPassword.equals(user.getPassword())) {
                passwordMatch = true;
            }
        }

        if (!passwordMatch) {
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác.");
        }

        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        user.setPassword(hashedPassword);
        userDAO.update(user);
    }
}
