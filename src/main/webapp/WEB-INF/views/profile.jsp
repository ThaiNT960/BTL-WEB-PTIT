<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="vi">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>PTIT Social - Trang Cá Nhân</title>
            <script src="https://cdn.tailwindcss.com"></script>
            <script>tailwind.config = { theme: { extend: { colors: { primary: '#ae1a21', 'primary-dark': '#8a141a' } } } }</script>
            <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
        </head>

        <body class="bg-gray-100 font-sans text-gray-900">

            <!-- NAVBAR -->
            <nav class="fixed top-0 left-0 right-0 z-50 bg-white border-b border-gray-200 shadow-sm"
                style="height:60px">
                <div class="max-w-5xl mx-auto px-5 h-full flex items-center justify-between gap-4">
                    <a href="${pageContext.request.contextPath}/HomeServlet"
                        class="flex items-center gap-2 text-primary font-bold text-lg">
                        <img src="${pageContext.request.contextPath}/img/logo_ptit.svg" alt="PTIT Logo"
                            class="w-10 h-10 object-contain"> PTIT Social
                    </a>
                    <div class="flex items-center gap-1">
                        <a href="${pageContext.request.contextPath}/HomeServlet" class="nav-link"><i
                                class="fas fa-home"></i> Trang chủ</a>
                        <a href="${pageContext.request.contextPath}/FriendServlet" class="nav-link"><i
                                class="fas fa-user-friends"></i> Bạn bè</a>
                        <a href="${pageContext.request.contextPath}/ChatServlet" class="nav-link"><i
                                class="fas fa-comment-dots"></i> Tin nhắn</a>
                    </div>
                    <div class="flex items-center gap-3 flex-shrink-0 relative">
                        <c:if test="${sessionScope.role == 'ROLE_ADMIN'}">
                            <a href="${pageContext.request.contextPath}/AdminServlet"
                                class="nav-link text-primary font-semibold"><i class="fas fa-shield-alt"></i> Admin</a>
                        </c:if>
                        <div class="relative" id="navAvatarWrap">
                            <button onclick="document.getElementById('userMenu').classList.toggle('hidden')"
                                class="w-10 h-10 bg-primary rounded-full flex items-center justify-center text-white font-bold text-base focus:outline-none">
                                ${empty sessionScope.fullName ? 'U' :
                                sessionScope.fullName.substring(0,1).toUpperCase()}
                            </button>
                            <div id="userMenu"
                                class="hidden absolute right-0 top-12 bg-white border border-gray-200 rounded-xl shadow-lg w-44 overflow-hidden z-50">
                                <a href="${pageContext.request.contextPath}/ProfileServlet"
                                    class="flex items-center gap-2 px-4 py-3 text-sm text-gray-700 hover:bg-gray-50"><i
                                        class="fas fa-user text-primary w-4"></i> Trang cá nhân</a>
                                <a href="${pageContext.request.contextPath}/LogoutServlet"
                                    class="flex items-center gap-2 px-4 py-3 text-sm text-gray-700 hover:bg-gray-50"><i
                                        class="fas fa-sign-out-alt text-primary w-4"></i> Đăng xuất</a>
                            </div>
                        </div>
                    </div>
                </div>
            </nav>

            <main class="pt-20 pb-10">
                <div class="max-w-3xl mx-auto px-4 space-y-4">

                    <!-- Profile Header -->
                    <div class="bg-white rounded-2xl shadow-sm overflow-hidden">
                        <div class="h-32 bg-gradient-to-r from-primary to-primary-dark"></div>
                        <div class="px-6 pb-6">
                            <div class="flex items-end gap-4 -mt-12 mb-4">
                                <div class="w-24 h-24 rounded-full bg-primary flex items-center justify-center text-white font-bold text-3xl border-4 border-white shadow-lg"
                                    id="profileAvatar">
                                    <c:choose>
                                        <c:when test="${not empty profileUser.avatar}">
                                            <img src="${profileUser.avatar}"
                                                class="w-full h-full object-cover rounded-full"
                                                onerror="this.parentElement.textContent='${empty profileUser.fullName ? 'U' : profileUser.fullName.substring(0,1).toUpperCase()}'" />
                                        </c:when>
                                        <c:otherwise>
                                            ${empty profileUser.fullName ? 'U' :
                                            profileUser.fullName.substring(0,1).toUpperCase()}
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="flex-1 mb-1">
                                    <h1 class="text-xl font-bold text-gray-900" id="profileName">${profileUser.fullName}
                                    </h1>
                                    <p class="text-sm text-gray-400" id="profileUsername">@${profileUser.username}</p>
                                </div>
                                <button onclick="document.getElementById('editModal').classList.remove('hidden')"
                                    class="flex items-center gap-2 text-sm bg-gray-100 hover:bg-gray-200 text-gray-700 font-medium px-4 py-2 rounded-full transition">
                                    <i class="fas fa-edit"></i> Chỉnh sửa
                                </button>
                            </div>
                        </div>
                    </div>

                    <!-- Posts -->
                    <div class="bg-white rounded-2xl shadow-sm p-5">
                        <h2 class="font-semibold text-gray-900 text-sm mb-4"><i
                                class="fas fa-th text-primary mr-2"></i>Bài viết của bạn</h2>
                        <div id="profilePosts">
                            <c:choose>
                                <c:when test="${empty userPosts}">
                                    <p class="text-center text-gray-400 text-sm py-6">Chưa có bài viết nào</p>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="post" items="${userPosts}">
                                        <div class="border border-gray-100 rounded-xl p-4 mb-4 last:mb-0">
                                            <p class="text-gray-800 text-sm leading-relaxed mb-2">${post.content}</p>
                                            <c:if test="${not empty post.imageUrl}">
                                                <img src="${post.imageUrl}"
                                                    class="w-full rounded-xl mb-2 max-h-60 object-cover">
                                            </c:if>
                                            <p class="text-xs text-gray-400">${post.likeCount} thích ·
                                                ${post.comments.size()} bình luận</p>
                                        </div>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </main>

            <!-- Edit Modal -->
            <div id="editModal"
                class="hidden fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                <div class="bg-white rounded-2xl shadow-xl p-6 w-full max-w-md">
                    <h3 class="font-bold text-gray-900 text-lg mb-4">Chỉnh sửa hồ sơ</h3>
                    <div class="space-y-4">
                        <div>
                            <label class="block text-gray-700 font-semibold mb-1 text-sm">Họ và tên</label>
                            <input type="text" id="editFullName" value="${profileUser.fullName}"
                                class="w-full px-4 py-2.5 border border-gray-200 rounded-xl focus:outline-none focus:border-primary text-sm transition">
                        </div>
                        <div>
                            <label class="block text-gray-700 font-semibold mb-1 text-sm">URL ảnh đại diện</label>
                            <input type="text" id="editAvatar" value="${profileUser.avatar}"
                                class="w-full px-4 py-2.5 border border-gray-200 rounded-xl focus:outline-none focus:border-primary text-sm transition"
                                placeholder="https://...">
                        </div>
                    </div>
                    <div class="flex gap-3 mt-6">
                        <button onclick="saveProfile()"
                            class="flex-1 bg-primary hover:bg-primary-dark text-white font-semibold py-2.5 rounded-xl transition text-sm">Lưu
                            thay đổi</button>
                        <button onclick="document.getElementById('editModal').classList.add('hidden')"
                            class="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold py-2.5 rounded-xl transition text-sm">Hủy</button>
                    </div>
                </div>
            </div>

            <script>
                var CTX = '${pageContext.request.contextPath}';
                function saveProfile() {
                    var fullName = document.getElementById('editFullName').value.trim();
                    var avatar = document.getElementById('editAvatar').value.trim();
                    if (!fullName) { alert('Vui lòng nhập tên'); return; }
                    var form = new FormData();
                    form.append('fullName', fullName);
                    form.append('avatar', avatar);
                    fetch(CTX + '/ProfileServlet', { method: 'POST', body: form })
                        .then(r => r.json())
                        .then(data => {
                            document.getElementById('editModal').classList.add('hidden');
                            document.getElementById('profileName').textContent = data.fullName;
                            alert('Đã cập nhật hồ sơ!');
                            location.reload();
                        }).catch(() => alert('Lỗi khi lưu'));
                }
            </script>
        </body>

        </html>