# Tổng kết quá trình Chuẩn hoá Authentication

Dưới đây là các thay đổi quan trọng giúp phần Authentication của Backend đạt được nhiều tiêu chuẩn của một ứng dụng thực tiễn:

## 1. Tách Biệt Tầng Logic (Service Layer) 
Thay vì trộn lẫn code tương tác cơ sở dữ liệu và xác thực nằm ngay trong `LoginServlet` và `RegisterServlet`, tôi đã đưa toàn bộ Logic này vào tệp tin **`AuthService.java`**.
- Lợi ích: Các Servlet giờ đây chỉ có nhiệm vụ rất đơn giản là tiếp nhận HTTP Request (`req.getParameter`), gọi tới `AuthService` xử lý, hứng `Exception` và điều hướng trả kết quả về View (`resp.sendRedirect`, `req.getRequestDispatcher`).

## 2. Validation Dữ Liệu Đầu Vào (Validation Layer)
Tạo công cụ Helper **`ValidationUtil.java`**:
- **Username**: Được quy định tối thiểu **8 ký tự** tới đa 50 ký tự (theo yêu cầu của bạn). Đồng thời tự động sử dụng Regex `^[a-zA-Z0-9_]{8,50}$` để ngăn chặn các ký tự lạ, dấu trống rỗng hay nháy đơn gây lỗi SQL hay hỏng format.
- **Password**: Được quy định phải nhập trên mức 6 ký tự.

## 3. Bảo mật Mật Khẩu với jBCrypt
Đã tích hợp thư viện Password Hashing chuẩn mực nhất của Java: `org.mindrot.jbcrypt`
- Xoá bỏ việc lưu trữ `password` trong CSDL dưới dạng text bình thường.
- Khi người dùng đăng ký, hệ thống gọi `BCrypt.hashpw(password, BCrypt.gensalt(12))` tạo ra mã hash.
- Khi đăng nhập, xác thực bằng `BCrypt.checkpw`.
- **(Đặc biệt)**: Trong `AuthService` đã có một Catch fallback nếu dữ liệu Password lấy ra từ CSDL không phải định dạng BCrypt (do account cũ test bằng Plaintext) thì Code vẫn hỗ trợ so khớp `.equals()`. Việc này giúp bạn không gặp phải lỗi khi Login bằng các User đã tạo từ trước.

## Cách tiếp theo (Next steps)
Bạn hãy khởi động lại Backend thông qua lệnh `mvnw.cmd tomcat7:run` và tận hưởng! Khi có lỗi do nhập username ngắn hơn 8 ký tự, màn hình Register sẽ hiển thị ngay lỗi tương ứng.
