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
                                            ${empty profileUser.fullName ? 'U' :
                                            profileUser.fullName.substring(0,1).toUpperCase()}
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="flex-1 mb-1">
                                    <h1 class="text-xl font-bold text-gray-900" id="profileName">${profileUser.fullName}
                                    </h1>
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
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Posts -->
                    <div id="profilePosts">
                        <c:choose>
                            <c:when test="${empty userPosts}">
                                <div class="bg-white rounded-2xl shadow-sm p-8 text-center text-gray-400 text-sm">Chưa có bài viết nào</div>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="post" items="${userPosts}">
                                    <div class="bg-white rounded-2xl shadow-sm mb-4 overflow-hidden border border-gray-100" id="post-${post.id}">
                                        <div class="p-5">
                                            <div class="flex items-center gap-3 mb-3">
                                                <c:choose>
                                                    <c:when test="${not empty post.user.avatar}">
                                                        <img src="${post.user.avatar}" class="w-10 h-10 rounded-full object-cover flex-shrink-0" onerror="this.style.display='none'">
                                                    </c:when>
                                                    <c:otherwise>
                                                        <div class="w-10 h-10 rounded-full bg-primary flex items-center justify-center text-white font-bold text-sm flex-shrink-0">${empty post.user.fullName ? 'U' : post.user.fullName.substring(0,1).toUpperCase()}</div>
                                                    </c:otherwise>
                                                </c:choose>
                                                <div class="flex-1 min-w-0">
                                                    <p class="font-semibold text-gray-900 text-sm">${post.user.fullName}</p>
                                                    <p class="text-xs text-gray-400">${post.createdAt}</p>
                                                </div>
                                                <button data-post-id="${post.id}" data-action="delete-post" class="js-post-action text-gray-300 hover:text-red-500 transition text-sm px-2"><i class="fas fa-trash"></i></button>
                                            </div>
                                            <p class="text-gray-800 text-sm leading-relaxed mb-3">${post.content}</p>
                                            <c:if test="${not empty post.imageUrl}">
                                                <img src="${post.imageUrl}" class="w-full rounded-xl mb-3 max-h-96 object-cover">
                                            </c:if>
                                            <div class="flex items-center gap-4 text-xs text-gray-400 mb-3">
                                                <span id="like-count-${post.id}">${post.likeCount} lượt thích</span>
                                                <span id="comment-count-${post.id}">${post.comments.size()} bình luận</span>
                                            </div>
                                        </div>
                                        <div class="border-t border-gray-100"></div>
                                        <div class="flex px-2 py-1">
                                            <button data-post-id="${post.id}" data-action="like" class="js-post-action flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl text-sm font-medium transition hover:bg-gray-50 text-gray-500">
                                                <i class="far fa-heart"></i> Thích
                                            </button>
                                            <button data-post-id="${post.id}" data-action="toggle-comments" class="js-post-action flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl text-sm font-medium text-gray-500 hover:bg-gray-50 transition">
                                                <i class="far fa-comment"></i> Bình luận
                                            </button>
                                        </div>
                                        <div id="comments-${post.id}" class="hidden border-t border-gray-100 p-4 bg-gray-50">
                                            <div id="comments-list-${post.id}">
                                                <c:forEach var="c" items="${post.comments}">
                                                    <div class="flex gap-3 mb-3">
                                                        <c:choose>
                                                            <c:when test="${not empty c.user.avatar}">
                                                                <img src="${c.user.avatar}" class="w-8 h-8 rounded-full object-cover flex-shrink-0" onerror="this.style.display='none'">
                                                            </c:when>
                                                            <c:otherwise>
                                                                <div class="w-8 h-8 rounded-full bg-primary flex items-center justify-center text-white font-bold text-xs flex-shrink-0">${empty c.user.fullName ? 'U' : c.user.fullName.substring(0,1).toUpperCase()}</div>
                                                            </c:otherwise>
                                                        </c:choose>
                                                        <div class="bg-white rounded-xl px-3 py-2 flex-1 shadow-sm">
                                                            <p class="font-semibold text-xs text-gray-700 mb-0.5">${c.user.fullName}</p>
                                                            <p class="text-sm text-gray-800">${c.content}</p>
                                                        </div>
                                                    </div>
                                                </c:forEach>
                                            </div>
                                            <div class="flex gap-2 mt-2">
                                                <input type="text" data-post-id="${post.id}" placeholder="Viết bình luận..." class="js-comment-input flex-1 bg-white border border-gray-200 rounded-full px-4 py-2 text-sm focus:outline-none focus:border-primary transition">
                                                <button data-post-id="${post.id}" data-action="submit-comment" class="js-post-action bg-primary hover:bg-primary-dark text-white text-xs font-semibold px-4 py-2 rounded-full transition">Gửi</button>
                                            </div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </div>
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
                var CTX = '${pageContext.request.contextPath}';
                var CURRENT_USER = {
                    username: '${sessionScope.username}',
                    fullName: '${sessionScope.fullName}',
                    avatar: '${sessionScope.avatar}',
                    role: '${sessionScope.role}'
                };
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
                    
                    fetch(CTX + '/ProfileServlet', { 
                        method: 'POST', 
                        body: formData 
                    }).then(res => res.json()).then(data => {
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
            <script src="${pageContext.request.contextPath}/js/api-client.js?v=2.0"></script>
            <script src="${pageContext.request.contextPath}/js/home-servlet.js?v=2.0"></script>
            <script>
                // Event delegation for post actions in profile page (data-action attributes)
                document.addEventListener('click', function (e) {
                    const btn = e.target.closest('.js-post-action');
                    if (!btn) return;
                    const postId = btn.dataset.postId;
                    const action = btn.dataset.action;
                    if (action === 'delete-post') deletePost(postId);
                    if (action === 'like') toggleLike(postId, btn);
                    if (action === 'toggle-comments') toggleComments(postId);
                    if (action === 'submit-comment') {
                        const inp = btn.parentElement.querySelector('.js-comment-input');
                        if (inp) submitCommentFrom(postId, inp);
                    }
                });
                document.addEventListener('keyup', function (e) {
                    if (e.key !== 'Enter') return;
                    const inp = e.target.closest('.js-comment-input');
                    if (!inp) return;
                    submitCommentFrom(inp.dataset.postId, inp);
                });
                function submitCommentFrom(postId, inp) {
                    var content = inp.value.trim();
                    if (!content) return;
                    var params = new URLSearchParams();
                    params.append('action', 'comment');
                    params.append('postId', postId);
                    params.append('content', content);
                    apiFetch(CTX + '/PostServlet', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                        body: params.toString()
                    }, false).then(function () {
                        inp.value = '';
                        var list = document.getElementById('comments-list-' + postId);
                        if (!list) return;
                        var d = document.createElement('div');
                        d.className = 'flex gap-3 mb-3';
                        var myInit = (CURRENT_USER.fullName || CURRENT_USER.username).charAt(0).toUpperCase();
                        var myAvt = CURRENT_USER.avatar
                            ? '<img src="' + CURRENT_USER.avatar + '" class="w-8 h-8 rounded-full object-cover flex-shrink-0" onerror="this.style.display=\'none\'">'
                            : '<div class="w-8 h-8 rounded-full bg-primary flex items-center justify-center text-white font-bold text-xs flex-shrink-0">' + myInit + '</div>';
                        d.innerHTML = myAvt + '<div class="bg-white rounded-xl px-3 py-2 flex-1 shadow-sm"><p class="font-semibold text-xs text-gray-700 mb-0.5">' + (CURRENT_USER.fullName || CURRENT_USER.username) + '</p><p class="text-sm text-gray-800" style="white-space: pre-wrap;">' + content + '</p></div>';
                        list.appendChild(d);
                        const countSpan = document.getElementById('comment-count-' + postId);
                        if (countSpan) {
                            var currentCount = parseInt(countSpan.textContent) || 0;
                            countSpan.textContent = (currentCount + 1) + ' bình luận';
                        }
                    });
                }
            </script>
        </body>

        </html>