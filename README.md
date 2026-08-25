# PTIT SOCIAL CHAT

Hệ thống Mạng xã hội & Chat trực tuyến dành cho sinh viên PTIT (Java Servlet/JSP, Hibernate, MySQL, WebSocket), tích hợp AI kiểm duyệt nội dung tự động (PhoBERT).

---

## 📦 1. Tải Tài Nguyên Từ Google Drive

Do giới hạn dung lượng lưu trữ trên GitHub (mô hình AI và dataset), các tài nguyên liên quan được lưu trữ trên Google Drive:

- 🔗 **Link Google Drive:** [Tải tài nguyên tại đây](https://drive.google.com/drive/folders/1vFz1GbVXCEycTC2VhGJ0c2w0U8OHbU03?usp=drive_link)
- **Tài nguyên trên Drive bao gồm:**
  - `ai-service/`: Microservice AI kiểm duyệt nội dung (FastAPI).
  - `ai_training_data/`: Dataset `.csv` và mã nguồn huấn luyện mô hình PhoBERT (`.ipynb`).

---

## 📁 2. Cấu Trúc Thư Mục Sau Khi Tải Về

Đặt các thư mục từ Google Drive cùng cấp với thư mục dự án Web:

```text
ptit-social-chat/
├── social_chat_web/          # Ứng dụng Web chính (Java Servlet/JSP + Hibernate + MySQL)
├── ai-service/               # Microservice AI kiểm duyệt nội dung (FastAPI + PhoBERT)
└── ai_training_data/         # Dataset (.csv) và mã nguồn huấn luyện PhoBERT (.ipynb)
```

---

## 🚀 3. Cách Khởi Chạy Hệ Thống

### 1. Khởi tạo Cơ Sở Dữ Liệu (MySQL)
- Mở MySQL và import file **`socialchat.sql`** để tạo database `ptitsocialchat` cùng các bảng liên quan.
- Kiểm tra cấu hình kết nối DB (URL, Username, Password) trong file:
  `src/main/resources/hibernate.cfg.xml` *(mặc định: `root` / `123456`)*.

### 2. Khởi chạy AI Service (FastAPI)
1. Mở Terminal tại thư mục `ai-service/`.
2. Cài đặt thư viện:
   ```bash
   pip install -r requirements.txt
   ```
3. Chạy service:
   ```bash
   uvicorn main:app --port 8000 --reload
   ```

### 3. Khởi chạy Web Application (Java Maven / Tomcat)
1. Mở Terminal / PowerShell tại thư mục `social_chat_web/`.
2. Chạy ứng dụng bằng Tomcat Maven Plugin:
   - **Windows:**
     ```bash
     .\mvnw.cmd tomcat7:run
     ```
   - **Linux / macOS:**
     ```bash
     ./mvnw tomcat7:run
     ```
   *(Hoặc import dự án vào IntelliJ IDEA / Eclipse và chạy với máy chủ Tomcat)*.

---

## 🌐 4. Truy Cập Hệ Thống

Sau khi khởi chạy thành công, mở trình duyệt và truy cập:
👉 **[http://localhost:8080/](http://localhost:8080/)** *(hoặc `http://localhost:8080/LoginServlet`)*

### 👥 Tài khoản thử nghiệm:
- **Tài khoản Quản trị (Admin):**
  - Username: `admin`
  - Password: `12345678` *(hệ thống tự động kích hoạt khi đăng nhập lần đầu)*
- **Tài khoản Sinh viên (User):** 
  - Người dùng có thể đăng ký tài khoản mới trực tiếp tại trang **Đăng ký** (`/RegisterServlet`).
  - *Quy tắc: Username từ 8 - 50 ký tự, Mật khẩu từ 6 ký tự.*

---

## 📸 5. Một Số Hình Ảnh Giao Diện Hệ Thống

### 1. Màn hình Đăng nhập & Đăng ký
![Giao diện Đăng nhập](https://via.placeholder.com/800x450.png?text=Giao+Dien+Dang+Nhap)
*Hình 1: Giao diện đăng nhập hệ thống PTIT Social Chat*

![Giao diện Đăng ký](https://via.placeholder.com/800x450.png?text=Giao+Dien+Dang+Ky)
*Hình 2: Màn hình đăng ký thành viên mới với kiểm tra độ mạnh mật khẩu và hợp lệ tài khoản*

---

### 2. Bảng tin (Newsfeed) & Đăng bài viết
![Bảng tin Newsfeed](https://via.placeholder.com/800x450.png?text=Bang+Tin+Newsfeed)
*Hình 3: Bảng tin hiển thị bài viết, tương tác Like, Bình luận đa tầng và Thông báo từ Admin*

---

### 3. Nhắn tin trực tuyến thời gian thực (Real-time Chat)
![Giao diện Chat trực tuyến](https://via.placeholder.com/800x450.png?text=Giao+Dien+Chat+Truc+Tuyen)
*Hình 4: Nhắn tin 1-1 trực tiếp qua giao thức WebSocket và gửi hình ảnh đính kèm*

---

### 4. Trang cá nhân & Quản lý bạn bè
![Trang cá nhân](https://via.placeholder.com/800x450.png?text=Trang+Ca+Nhan)
*Hình 5: Quản lý thông tin cá nhân, cập nhật ảnh đại diện và đổi mật khẩu*

![Danh sách bạn bè](https://via.placeholder.com/800x450.png?text=Danh+Sach+Ban+Be)
*Hình 6: Tìm kiếm người dùng, gửi/chấp nhận lời mời kết bạn*

---

### 5. Bảng điều khiển Quản trị (Admin) & Tích hợp AI Kiểm duyệt
![Admin Dashboard](https://via.placeholder.com/800x450.png?text=Admin+Dashboard)
*Hình 7: Thống kê số lượng người dùng, bài viết, gửi thông báo toàn hệ thống*

![Kiểm duyệt nội dung tự động bằng AI](https://via.placeholder.com/800x450.png?text=AI+Moderation+PhoBERT)
*Hình 8: Hệ thống AI PhoBERT tự động phân loại văn bản độc hại (Toxicity) và cảnh báo/chặn bài viết vi phạm*

---

![Visitors](https://komarev.com/ghpvc/?username=ThaiNT960-4&repo=TTCS-PTIT&color=blue&style=flat-square)
