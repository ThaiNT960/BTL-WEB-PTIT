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
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css?v=3.1">
        </head>

        <body class="bg-gray-100 font-sans text-gray-900"
              data-username="<c:out value='${sessionScope.username}'/>"
              data-fullname="<c:out value='${sessionScope.fullName}'/>"
              data-avatar="<c:out value='${sessionScope.avatar}'/>"
              data-role="<c:out value='${sessionScope.role}'/>"
              data-ctx="<c:out value='${pageContext.request.contextPath}'/>"
              data-csrftoken="<c:out value='${sessionScope.csrfToken}'/>">

            <!-- NAVBAR -->
            <!-- NAVBAR -->
            <jsp:include page="/WEB-INF/views/layout/navbar.jsp">
                <jsp:param name="activeMenu" value="profile" />
            </jsp:include>

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
                                                onerror="this.style.display='none'" />
                                        </c:when>
                                        <c:otherwise>
                                            <c:out value="${empty profileUser.fullName ? profileUser.username.substring(0,1).toUpperCase() : profileUser.fullName.substring(0,1).toUpperCase()}" />
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="flex-1 mb-1">
                                    <h1 class="text-xl font-bold text-gray-900" id="profileName"><c:out value="${profileUser.fullName}" /></h1>
                                </div>
                                <div class="flex gap-2">
                                <c:if test="${profileUser.id == sessionScope.userId}">
                                    <button onclick="document.getElementById('editModal').classList.remove('hidden')"
                                        class="flex items-center gap-2 text-sm bg-gray-100 hover:bg-gray-200 text-gray-700 font-medium px-4 py-2 rounded-full transition">
                                        <i class="fas fa-edit"></i> Chỉnh sửa
                                    </button>
                                    <button onclick="document.getElementById('passwordModal').classList.remove('hidden')"
                                        class="flex items-center gap-2 text-sm bg-primary hover:bg-primary-dark text-white font-medium px-4 py-2 rounded-full transition">
                                        <i class="fas fa-key"></i> Đổi mật khẩu
                                    </button>
                                </c:if>
                                <c:if test="${profileUser.id != sessionScope.userId}">
                                    <%-- Friend action buttons --%>
                                    <div id="friendActionArea">
                                        <c:choose>
                                            <c:when test="${relationshipStatus == 'FRIENDS'}">
                                                <div class="flex gap-2">
                                                    <a href="${pageContext.request.contextPath}/chat?chatWith=${profileUser.username}"
                                                        class="flex items-center gap-2 text-sm bg-gray-100 hover:bg-gray-200 text-gray-600 font-medium px-4 py-2 rounded-full transition">
                                                        <i class="fas fa-comment-dots"></i> Nhắn tin
                                                    </a>
                                                    <button data-username="${profileUser.username}" data-action="unfriend"
                                                        class="js-friend-action flex items-center gap-2 text-sm bg-red-50 hover:bg-red-100 text-red-600 font-medium px-4 py-2 rounded-full transition">
                                                        <i class="fas fa-user-minus"></i> Hủy kết bạn
                                                    </button>
                                                </div>
                                            </c:when>
                                            <c:when test="${relationshipStatus == 'PENDING_SENT'}">
                                                <button data-username="${profileUser.username}" data-action="cancel"
                                                    class="js-friend-action flex items-center gap-2 text-sm bg-gray-100 hover:bg-gray-200 text-gray-500 font-medium px-4 py-2 rounded-full transition">
                                                    <i class="fas fa-clock"></i> Thu hồi lời mời
                                                </button>
                                            </c:when>
                                            <c:when test="${relationshipStatus == 'PENDING_RECEIVED'}">
                                                <div class="flex gap-2">
                                                    <button data-req-id="${friendRequestId}" data-action="accept"
                                                        class="js-friend-action flex items-center gap-2 text-sm bg-primary hover:bg-primary-dark text-white font-semibold px-4 py-2 rounded-full transition">
                                                        <i class="fas fa-check"></i> Chấp nhận
                                                    </button>
                                                    <button data-req-id="${friendRequestId}" data-action="reject"
                                                        class="js-friend-action flex items-center gap-2 text-sm bg-gray-100 hover:bg-gray-200 text-gray-600 font-medium px-4 py-2 rounded-full transition">
                                                        <i class="fas fa-times"></i> Từ chối
                                                    </button>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <button onclick="sendRequest('${profileUser.username}', this)"
                                                    class="flex items-center gap-2 text-sm bg-primary hover:bg-primary-dark text-white font-semibold px-4 py-2 rounded-full transition">
                                                    <i class="fas fa-user-plus"></i> Kết bạn
                                                </button>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </c:if>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Posts -->
                    <div id="postsFeed"></div>
                </div>
            </main>

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
                            <label class="block text-gray-700 font-semibold mb-1 text-sm">Ảnh đại diện mới</label>
                            <input type="file" id="editAvatarFile" accept="image/*"
                                class="w-full px-4 py-2 border border-gray-200 rounded-xl focus:outline-none focus:border-primary text-sm transition">
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

            <!-- Password Modal -->
            <div id="passwordModal"
                class="hidden fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                <div class="bg-white rounded-2xl shadow-xl p-6 w-full max-w-md">
                    <h3 class="font-bold text-gray-900 text-lg mb-4">Đổi mật khẩu</h3>
                    <div class="space-y-4">
                        <div>
                            <label class="block text-gray-700 font-semibold mb-1 text-sm">Mật khẩu cũ</label>
                            <input type="password" id="oldPassword"
                                class="w-full px-4 py-2.5 border border-gray-200 rounded-xl focus:outline-none focus:border-primary text-sm transition">
                        </div>
                        <div>
                            <label class="block text-gray-700 font-semibold mb-1 text-sm">Mật khẩu mới</label>
                            <input type="password" id="newPassword"
                                class="w-full px-4 py-2.5 border border-gray-200 rounded-xl focus:outline-none focus:border-primary text-sm transition">
                        </div>
                    </div>
                    <div class="flex gap-3 mt-6">
                        <button onclick="changePassword()"
                            class="flex-1 bg-primary hover:bg-primary-dark text-white font-semibold py-2.5 rounded-xl transition text-sm">Xác nhận</button>
                        <button onclick="document.getElementById('passwordModal').classList.add('hidden')"
                            class="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold py-2.5 rounded-xl transition text-sm">Hủy</button>
                    </div>
                </div>
            </div>

            <script>
                // Read from body dataset
                var PROFILE_USERNAME = '<c:out value="${profileUser.username}"/>';
                function saveProfile() {
                    var fullName = document.getElementById('editFullName').value.trim();
                    var avatarFileInput = document.getElementById('editAvatarFile');
                    if (!fullName) { alert('Vui lòng nhập tên'); return; }
                    var formData = new FormData();
                    formData.append('action', 'update');
                    formData.append('fullName', fullName);
                    if (avatarFileInput.files.length > 0) {
                        formData.append('avatarFile', avatarFileInput.files[0]);
                    }
                    
                    apiFetch(CTX + '/ProfileServlet', { 
                        method: 'POST', 
                        body: formData 
                    }, false).then(res => res.json()).then(data => {
                            if (data.error) {
                                alert(data.error);
                                return;
                            }
                            document.getElementById('editModal').classList.add('hidden');
                            document.getElementById('profileName').textContent = data.fullName;
                            alert('Đã cập nhật hồ sơ!');
                            location.reload();
                    }).catch(err => {
                        console.error('Error fetching', err);
                        alert('Đã có lỗi xảy ra!');
                    });
                }

                function changePassword() {
                    var oldPassword = document.getElementById('oldPassword').value;
                    var newPassword = document.getElementById('newPassword').value;
                    if (!oldPassword || !newPassword) { alert('Vui lòng nhập đầy đủ'); return; }
                    var params = new URLSearchParams();
                    params.append('action', 'change_password');
                    params.append('oldPassword', oldPassword);
                    params.append('newPassword', newPassword);
                    apiFetch(CTX + '/ProfileServlet', { 
                        method: 'POST', 
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                        body: params.toString() 
                    }).then(data => {
                        if(data && data.status === 'ok') {
                            alert('Đổi mật khẩu thành công!');
                            document.getElementById('passwordModal').classList.add('hidden');
                            document.getElementById('oldPassword').value = '';
                            document.getElementById('newPassword').value = '';
                        }
                    });
                }
            </script>
            <script src="${pageContext.request.contextPath}/js/api-client.js?v=3.1"></script>
            <c:if test="${profileUser.id != sessionScope.userId}">
                <script src="${pageContext.request.contextPath}/js/friend-servlet.js?v=3.1"></script>
            </c:if>
            <script src="${pageContext.request.contextPath}/js/home-servlet.js?v=3.1"></script>

        </body>

        </html>