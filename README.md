# PTIT SOCIAL CHAT

Hệ thống Mạng xã hội & Chat trực tuyến dành cho sinh viên PTIT (Java Servlet/JSP, Hibernate, MySQL, WebSocket), tích hợp AI kiểm duyệt nội dung tự động (PhoBERT).

---

##  1. Tải Tài Nguyên Từ Google Drive

Do giới hạn dung lượng lưu trữ trên GitHub (mô hình AI và dataset), các tài nguyên liên quan được lưu trữ trên Google Drive:

-  **Link Google Drive:** [Tải tài nguyên tại đây](https://drive.google.com/drive/folders/1vFz1GbVXCEycTC2VhGJ0c2w0U8OHbU03?usp=drive_link)
- **Tài nguyên trên Drive bao gồm:**
  - `ai-service/`: Microservice AI kiểm duyệt nội dung (FastAPI).
  - `ai_training_data/`: Dataset `.csv` và mã nguồn huấn luyện mô hình PhoBERT (`.ipynb`).

---

##  2. Cấu Trúc Thư Mục Sau Khi Tải Về

Đặt các thư mục từ Google Drive cùng cấp với thư mục dự án Web:

```text
ptit-social-chat/
├── social_chat_web/          # Ứng dụng Web chính (Java Servlet/JSP + Hibernate + MySQL)
├── ai-service/               # Microservice AI kiểm duyệt nội dung (FastAPI + PhoBERT)
└── ai_training_data/         # Dataset (.csv) và mã nguồn huấn luyện PhoBERT (.ipynb)
```

---

##  3. Cách Khởi Chạy Hệ Thống

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

##  4. Truy Cập Hệ Thống

Sau khi khởi chạy thành công, mở trình duyệt và truy cập:
 **[http://localhost:8080/](http://localhost:8080/)** *(hoặc `http://localhost:8080/LoginServlet`)*

###  Tài khoản thử nghiệm:
- **Tài khoản Quản trị (Admin):**
  - Username: `admin`
  - Password: `12345678` *(hệ thống tự động kích hoạt khi đăng nhập lần đầu)*
- **Tài khoản Sinh viên (User):** 
  - Người dùng có thể đăng ký tài khoản mới trực tiếp tại trang **Đăng ký** (`/RegisterServlet`).
  - *Quy tắc: Username từ 8 - 50 ký tự, Mật khẩu từ 6 ký tự.*

---

##  5. Một Số Hình Ảnh Giao Diện Hệ Thống

### 1. Màn hình Đăng nhập & Đăng ký
<p align="center">
  <img width="700" alt="Giao diện Đăng nhập" src="https://github.com/user-attachments/assets/89fa5dcc-eb8c-4f01-aafa-a82477fcb4d3" />
</p>
<p align="center"><em>Hình 1: Giao diện đăng nhập hệ thống PTIT Social Chat</em></p>

<p align="center">
  <img width="700" alt="Giao diện Đăng ký" src="https://github.com/user-attachments/assets/ff541cca-0566-4dc8-861d-6ddd25c9d775" />
</p>
<p align="center"><em>Hình 2: Màn hình đăng ký thành viên mới với kiểm tra độ mạnh mật khẩu và hợp lệ tài khoản</em></p>



### 2. Bảng tin (Newsfeed) & Đăng bài viết
<p align="center">
  <img width="700" alt="Bảng tin Newsfeed" src="https://github.com/user-attachments/assets/8e88059e-ebbd-4572-9c4e-d43e657d3b2c" />
</p>
<p align="center"><em>Hình 3: Bảng tin hiển thị bài viết, tương tác Like, Bình luận đa tầng và Thông báo từ Admin</em></p>



### 3. Nhắn tin trực tuyến thời gian thực (Real-time Chat)
<p align="center">
  <img width="700" alt="Giao diện Chat trực tuyến" src="https://github.com/user-attachments/assets/4c4bea12-66b8-463d-a1bc-5a01d3a66bce" />
</p>
<p align="center"><em>Hình 4: Nhắn tin 1-1 trực tiếp qua giao thức WebSocket và gửi hình ảnh đính kèm</em></p>



### 4. Trang cá nhân & Quản lý bạn bè
<p align="center">
  <img width="700" alt="Trang cá nhân" src="https://github.com/user-attachments/assets/bd72442f-61fd-4b60-a55a-8d36508dfab6" />
</p>
<p align="center"><em>Hình 5: Quản lý thông tin cá nhân, cập nhật ảnh đại diện và đổi mật khẩu</em></p>

<p align="center">
  <img width="700" alt="Danh sách bạn bè" src="https://github.com/user-attachments/assets/d601f0f3-454e-4216-8ff6-7f5bf8a828e7" />
</p>
<p align="center"><em>Hình 6: Tìm kiếm người dùng, gửi/chấp nhận lời mời kết bạn</em></p>



### 5. Bảng điều khiển Quản trị (Admin) & Tích hợp AI Kiểm duyệt
<p align="center">
  <img width="700" alt="Admin Dashboard" src="https://github.com/user-attachments/assets/5d9e7d27-b475-4ad6-8e44-194885698c14" />
</p>
<p align="center"><em>Hình 7: Thống kê số lượng người dùng, bài viết, gửi thông báo toàn hệ thống</em></p>

<p align="center">
  <img width="700" alt="Kiểm duyệt nội dung tự động bằng AI" src="https://github.com/user-attachments/assets/741e3fe9-7191-46aa-b5da-86c22b69530d" />
</p>
<p align="center"><em>Hình 8: Hệ thống AI PhoBERT tự động phân loại văn bản độc hại (Toxicity) và cảnh báo/chặn bài viết vi phạm</em></p>



![Visitors](https://komarev.com/ghpvc/?username=ThaiNT960-4&repo=TTCS-PTIT&color=blue&style=flat-square)
