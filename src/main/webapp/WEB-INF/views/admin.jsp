<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="vi">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>PTIT Social - Quản Trị</title>
            <script src="https://cdn.tailwindcss.com"></script>
            <script>tailwind.config = { theme: { extend: { colors: { primary: '#ae1a21', 'primary-dark': '#8a141a' } } } }</script>
            <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
        </head>

        <body class="bg-gray-100 font-sans text-gray-900"
              data-username="<c:out value='${sessionScope.username}'/>"
              data-fullname="<c:out value='${sessionScope.fullName}'/>"
              data-avatar="<c:out value='${sessionScope.avatar}'/>"
              data-role="<c:out value='${sessionScope.role}'/>"
              data-ctx="<c:out value='${pageContext.request.contextPath}'/>"
              data-csrftoken="<c:out value='${sessionScope.csrfToken}'/>">

            <!-- NAVBAR -->
            <nav class="fixed top-0 left-0 right-0 z-50 bg-white border-b border-gray-200 shadow-sm"
                style="height:60px">
                <div class="max-w-6xl mx-auto px-5 h-full flex items-center justify-between gap-4">
                    <a href="${pageContext.request.contextPath}/HomeServlet"
                        class="flex items-center gap-2 text-primary font-bold text-lg">
                        <img src="${pageContext.request.contextPath}/img/logo_ptit.svg" alt="PTIT Logo"
                            class="w-10 h-10 object-contain"> PTIT Social
                    </a>
                    <span class="font-semibold text-primary text-sm"><i class="fas fa-shield-alt mr-1"></i>Bảng Quản
                        Trị</span>
                    <div class="flex items-center gap-3">
                        <a href="${pageContext.request.contextPath}/LogoutServlet"
                            class="text-sm text-gray-600 hover:text-primary"><i
                                class="fas fa-sign-out-alt mr-1"></i>Đăng xuất</a>
                    </div>
                </div>
            </nav>

            <main class="pt-20 pb-10">
                <div class="max-w-6xl mx-auto px-4 space-y-6">

                    <!-- Stats Cards -->
                    <div class="grid grid-cols-2 gap-4">
                        <div class="bg-white rounded-2xl shadow-sm p-5 flex items-center gap-4">
                            <div class="w-12 h-12 bg-red-50 rounded-xl flex items-center justify-center">
                                <i class="fas fa-users text-primary text-xl"></i>
                            </div>
                            <div>
                                <p class="text-gray-500 text-sm">Tổng người dùng</p>
                                <p class="font-bold text-2xl text-gray-900" id="statUsers">${users.size()}</p>
                            </div>
                        </div>
                        <div class="bg-white rounded-2xl shadow-sm p-5 flex items-center gap-4">
                            <div class="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center">
                                <i class="fas fa-file-alt text-blue-500 text-xl"></i>
                            </div>
                            <div>
                                <p class="text-gray-500 text-sm">Tổng bài viết</p>
                                <p class="font-bold text-2xl text-gray-900">${posts.size()}</p>
                            </div>
                        </div>
                    </div>

                    <!-- Users Table -->
                    <div class="bg-white rounded-2xl shadow-sm p-5">
                        <div class="flex items-center justify-between mb-4">
                            <h2 class="font-semibold text-gray-900"><i class="fas fa-users text-primary mr-2"></i>Danh
                                sách người dùng</h2>
                            <button onclick="document.getElementById('addUserModal').classList.remove('hidden')"
                                class="text-xs bg-primary hover:bg-primary-dark text-white font-semibold px-4 py-2 rounded-full transition">
                                <i class="fas fa-plus mr-1"></i>Thêm user
                            </button>
                        </div>
                        <div class="overflow-x-auto">
                            <table class="w-full">
                                <thead>
                                    <tr class="border-b border-gray-100">
                                        <th class="text-left text-xs font-semibold text-gray-400 pb-3 pr-4">ID</th>
                                        <th class="text-left text-xs font-semibold text-gray-400 pb-3 pr-4">Tên</th>
                                        <th class="text-left text-xs font-semibold text-gray-400 pb-3 pr-4">Username
                                        </th>
                                        <th class="text-left text-xs font-semibold text-gray-400 pb-3 pr-4">Vai trò</th>
                                        <th class="text-left text-xs font-semibold text-gray-400 pb-3">Hành động</th>
                                    </tr>
                                </thead>
                                <tbody id="usersTableBody">
                                    <c:forEach var="u" items="${users}">
                                        <tr class="border-b border-gray-50 hover:bg-gray-50" id="user-row-${u.id}">
                                            <td class="py-3 pr-4 text-sm text-gray-500">${u.id}</td>
                                            <td class="py-3 pr-4">
                                                <div class="flex items-center gap-2">
                                                    <div
                                                        class="w-8 h-8 bg-primary rounded-full flex items-center justify-center text-white text-xs font-bold flex-shrink-0">
                                                        ${empty u.fullName ? u.username.substring(0,1).toUpperCase() :
                                                        u.fullName.substring(0,1).toUpperCase()}
                                                    </div>
                                                    <span class="text-sm font-medium text-gray-900">${u.fullName}</span>
                                                </div>
                                            </td>
                                            <td class="py-3 pr-4 text-sm text-gray-600">@${u.username}</td>
                                            <td class="py-3 pr-4">
                                                <span
                                                    class="text-xs font-semibold px-2.5 py-1 rounded-full ${u.role == 'ROLE_ADMIN' ? 'bg-red-50 text-primary' : 'bg-gray-100 text-gray-500'}">
                                                    ${u.role == 'ROLE_ADMIN' ? 'Admin' : 'User'}
                                                </span>
                                            </td>
                                            <td class="py-3">
                                                <button onclick="deleteUser(${u.id})"
                                                    class="text-xs text-gray-400 hover:text-red-500 transition px-2 py-1 rounded-lg hover:bg-red-50">
                                                    <i class="fas fa-trash"></i> Xóa
                                                </button>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- Posts Table -->
                    <div class="bg-white rounded-2xl shadow-sm p-5 mt-6">
                        <div class="flex items-center justify-between mb-4">
                            <h2 class="font-semibold text-gray-900"><i class="fas fa-file-alt text-primary mr-2"></i>Danh sách bài viết</h2>
                        </div>
                        <div class="overflow-x-auto">
                            <table class="w-full">
                                <thead>
                                    <tr class="border-b border-gray-100">
                                        <th class="text-left text-xs font-semibold text-gray-400 pb-3 pr-4">ID</th>
                                        <th class="text-left text-xs font-semibold text-gray-400 pb-3 pr-4">Tác giả</th>
                                        <th class="text-left text-xs font-semibold text-gray-400 pb-3 pr-4">Nội dung</th>
                                        <th class="text-left text-xs font-semibold text-gray-400 pb-3 pr-4">Thời gian</th>
                                        <th class="text-left text-xs font-semibold text-gray-400 pb-3">Hành động</th>
                                    </tr>
                                </thead>
                                <tbody id="postsTableBody">
                                    <c:forEach var="p" items="${posts}">
                                        <tr class="border-b border-gray-50 hover:bg-gray-50" id="post-row-${p.id}">
                                            <td class="py-3 pr-4 text-sm text-gray-500">${p.id}</td>
                                            <td class="py-3 pr-4 text-sm font-medium text-gray-900">${p.user.fullName}</td>
                                            <td class="py-3 pr-4 text-sm text-gray-600 max-w-xs truncate">${p.content}</td>
                                            <td class="py-3 pr-4 text-sm text-gray-500">${p.createdAt}</td>
                                            <td class="py-3">
                                                <button onclick="deletePost(${p.id})"
                                                    class="text-xs text-gray-400 hover:text-red-500 transition px-2 py-1 rounded-lg hover:bg-red-50">
                                                    <i class="fas fa-trash"></i> Xóa
                                                </button>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </main>

            <!-- Add User Modal -->
            <div id="addUserModal"
                class="hidden fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                <div class="bg-white rounded-2xl shadow-xl p-6 w-full max-w-sm">
                    <h3 class="font-bold text-gray-900 text-lg mb-4">Thêm người dùng mới</h3>
                    <div class="space-y-3">
                        <input type="text" id="newUsername" placeholder="Username"
                            class="w-full px-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-primary">
                        <input type="text" id="newFullName" placeholder="Họ và tên"
                            class="w-full px-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-primary">
                        <input type="password" id="newPassword" placeholder="Mật khẩu"
                            class="w-full px-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-primary">
                    </div>
                    <div class="flex gap-3 mt-5">
                        <button onclick="createUser()"
                            class="flex-1 bg-primary hover:bg-primary-dark text-white font-semibold py-2.5 rounded-xl text-sm transition">Tạo</button>
                        <button onclick="document.getElementById('addUserModal').classList.add('hidden')"
                            class="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold py-2.5 rounded-xl text-sm transition">Hủy</button>
                    </div>
                </div>
            </div>

            <script>
                // CTX is from body dataset

                function deleteUser(userId) {
                    if (!confirm('Xóa người dùng này? Thao tác này sẽ xóa mọi dữ liệu liên quan!')) return;
                    var params = new URLSearchParams();
                    params.append('action', 'deleteUser');
                    params.append('userId', userId);
                    fetch(CTX + '/AdminServlet', { 
                        method: 'POST', 
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                        body: params.toString() 
                    }).then(r => r.json()).then(data => {
                        if(data.error) {
                            alert(data.error);
                            return;
                        }
                        document.getElementById('user-row-' + userId).remove();
                        var stat = document.getElementById('statUsers');
                        stat.textContent = parseInt(stat.textContent) - 1;
                    }).catch(e => { console.error(e); alert('Lỗi xóa') });
                }

                function deletePost(postId) {
                    if (!confirm('Xóa bài viết này?')) return;
                    var params = new URLSearchParams();
                    params.append('action', 'deletePost');
                    params.append('postId', postId);
                    fetch(CTX + '/AdminServlet', { 
                        method: 'POST', 
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                        body: params.toString() 
                    }).then(r => r.json()).then(data => {
                        if(data.error) {
                            alert(data.error);
                            return;
                        }
                        document.getElementById('post-row-' + postId).remove();
                    }).catch(e => { console.error(e); alert('Lỗi xóa') });
                }

                function createUser() {
                    var username = document.getElementById('newUsername').value.trim();
                    var fullName = document.getElementById('newFullName').value.trim();
                    var password = document.getElementById('newPassword').value.trim();
                    if (!username || !fullName || !password) { alert('Vui lòng điền đầy đủ'); return; }
                    var params = new URLSearchParams();
                    params.append('action', 'createUser');
                    params.append('username', username);
                    params.append('fullName', fullName);
                    params.append('password', password);
                    fetch(CTX + '/AdminServlet', { 
                        method: 'POST', 
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                        body: params.toString() 
                    })
                    .then(async r => {
                        if (!r.ok) {
                            const data = await r.json();
                            throw new Error(data.error || 'Server error');
                        }
                        return r.json();
                    })
                    .then(data => {
                        document.getElementById('addUserModal').classList.add('hidden');
                        alert('Tạo người dùng thành công!');
                        location.reload();
                    }).catch(err => alert(err.message));
                }
            </script>
        </body>

        </html>