<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="vi">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>PTIT Social - Bạn Bè</title>
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
                        <a href="${pageContext.request.contextPath}/FriendServlet" class="nav-link active"><i
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
                                class="w-10 h-10 bg-primary rounded-full flex items-center justify-center text-white font-bold text-base focus:outline-none"
                                id="navAvatar">
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

                    <!-- Search Friends -->
                    <div class="bg-white rounded-2xl shadow-sm p-5">
                        <h2 class="font-semibold text-gray-900 text-sm mb-3"><i
                                class="fas fa-search text-primary mr-2"></i>Tìm kiếm bạn bè</h2>
                        <div class="flex gap-2">
                            <input type="text" id="searchInput" placeholder="Nhập tên hoặc username..."
                                class="flex-1 border border-gray-200 rounded-full px-4 py-2 text-sm focus:outline-none focus:border-primary transition"
                                onkeyup="if(event.key==='Enter') searchFriend()">
                            <button onclick="searchFriend()"
                                class="bg-primary hover:bg-primary-dark text-white text-sm font-semibold px-4 py-2 rounded-full transition">Tìm</button>
                        </div>
                        <div id="searchResults" class="mt-3"></div>
                    </div>

                    <!-- Friend Requests -->
                    <div class="bg-white rounded-2xl shadow-sm p-5">
                        <h2 class="font-semibold text-gray-900 text-sm mb-3"><i
                                class="fas fa-user-clock text-primary mr-2"></i>Lời mời kết bạn</h2>
                        <div id="friendRequests">
                            <c:choose>
                                <c:when test="${empty friendRequests}">
                                    <p class="text-center text-gray-400 text-sm py-6">Không có lời mời kết bạn nào</p>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="req" items="${friendRequests}">
                                        <div
                                            class="flex items-center gap-3 py-3 border-b border-gray-100 last:border-0">
                                            <div
                                                class="w-11 h-11 rounded-full bg-primary flex items-center justify-center text-white font-bold flex-shrink-0">
                                                ${empty req.sender.fullName ?
                                                req.sender.username.substring(0,1).toUpperCase() :
                                                req.sender.fullName.substring(0,1).toUpperCase()}
                                            </div>
                                            <div class="flex-1 min-w-0">
                                                <p class="font-semibold text-sm text-gray-900">${req.sender.fullName}
                                                </p>
                                                <p class="text-xs text-gray-400">@${req.sender.username}</p>
                                            </div>
                                            <div class="flex gap-2">
                                                <button onclick="acceptRequest(${req.id}, this)"
                                                    class="text-xs bg-primary hover:bg-primary-dark text-white font-semibold px-3 py-1.5 rounded-full transition">Chấp
                                                    nhận</button>
                                                <button onclick="rejectRequest(${req.id}, this)"
                                                    class="text-xs bg-gray-100 hover:bg-gray-200 text-gray-600 font-medium px-3 py-1.5 rounded-full transition">Từ
                                                    chối</button>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <!-- Friends List -->
                    <div class="bg-white rounded-2xl shadow-sm p-5">
                        <h2 class="font-semibold text-gray-900 text-sm mb-3"><i
                                class="fas fa-user-friends text-primary mr-2"></i>Bạn bè (
                            <c:out value="${friends.size()}" />)
                        </h2>
                        <div id="friendsList">
                            <c:choose>
                                <c:when test="${empty friends}">
                                    <p class="text-center text-gray-400 text-sm py-6">Bạn chưa có bạn bè nào</p>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="f" items="${friends}">
                                        <div
                                            class="flex items-center gap-3 py-3 border-b border-gray-100 last:border-0">
                                            <div
                                                class="w-11 h-11 rounded-full bg-primary flex items-center justify-center text-white font-bold flex-shrink-0">
                                                ${empty f.fullName ? f.username.substring(0,1).toUpperCase() :
                                                f.fullName.substring(0,1).toUpperCase()}
                                            </div>
                                            <div class="flex-1 min-w-0">
                                                <p class="font-semibold text-sm text-gray-900">${f.fullName}</p>
                                                <p class="text-xs text-gray-400">@${f.username}</p>
                                            </div>
                                            <a href="${pageContext.request.contextPath}/ChatServlet?chatWith=${f.username}"
                                                class="text-xs bg-gray-100 hover:bg-gray-200 text-gray-600 font-medium px-3 py-1.5 rounded-full transition">
                                                <i class="fas fa-comment-dots"></i> Nhắn tin
                                            </a>
                                        </div>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </main>

            <script>
                var CTX = '${pageContext.request.contextPath}';
                var CURRENT_USER = { username: '${sessionScope.username}' };
            </script>
            <script src="${pageContext.request.contextPath}/js/friend-servlet.js"></script>
        </body>

        </html>