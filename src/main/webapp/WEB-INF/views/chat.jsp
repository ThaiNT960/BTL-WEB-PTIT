<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="vi">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>PTIT Social - Tin Nhắn</title>
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
                        <a href="${pageContext.request.contextPath}/ChatServlet" class="nav-link active"><i
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

            <!-- CHAT LAYOUT -->
            <main class="pt-16" style="height:100vh; display:flex; overflow:hidden;">
                <!-- Friends Sidebar -->
                <div class="w-80 flex-shrink-0 bg-white border-r border-gray-200 flex flex-col"
                    style="height:calc(100vh - 60px); margin-top:4px;">
                    <div class="p-4 border-b border-gray-100">
                        <h2 class="font-semibold text-gray-900 text-sm">Tin nhắn</h2>
                    </div>
                    <div id="friendsList" class="flex-1 overflow-y-auto p-2">
                        <c:choose>
                            <c:when test="${empty friends}">
                                <p class="text-center text-gray-400 text-sm py-8">Chưa có bạn bè.<br>Hãy thêm bạn bè
                                    trước!</p>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="f" items="${friends}">
                                    <div class="chat-contact-item flex items-center gap-3 p-3 rounded-xl cursor-pointer hover:bg-gray-50 transition"
                                        data-username="${f.username}" data-fullname="${f.fullName}"
                                        onclick="selectChat('${f.username}', '${f.fullName}')">
                                        <div
                                            class="w-10 h-10 rounded-full bg-primary flex items-center justify-center text-white font-bold text-sm flex-shrink-0">
                                            ${empty f.fullName ? f.username.substring(0,1).toUpperCase() :
                                            f.fullName.substring(0,1).toUpperCase()}
                                        </div>
                                        <div class="flex-1 min-w-0">
                                            <p class="font-semibold text-sm text-gray-900 truncate">${f.fullName}</p>
                                            <p class="text-xs text-gray-400 truncate">@${f.username}</p>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <!-- Chat Window -->
                <div class="flex-1 flex flex-col" style="height:calc(100vh - 60px); margin-top:4px;">
                    <!-- Chat header -->
                    <div id="chatWindowHeader"
                        class="hidden px-5 py-3 bg-white border-b border-gray-200 flex items-center gap-3">
                        <div class="w-10 h-10 rounded-full bg-primary flex items-center justify-center text-white font-bold text-sm"
                            id="chatPartnerAvatar">?</div>
                        <div>
                            <p class="font-semibold text-sm text-gray-900" id="chatTitle">Chọn cuộc trò chuyện</p>
                            <p class="text-xs text-gray-400">Online</p>
                        </div>
                    </div>
                    <div id="chatPlaceholder" class="flex-1 flex items-center justify-center text-gray-400 text-sm">
                        <div class="text-center">
                            <i class="fas fa-comment-dots text-4xl mb-3 opacity-30"></i>
                            <p>Chọn một người bạn để bắt đầu trò chuyện</p>
                        </div>
                    </div>
                    <!-- Messages area -->
                    <div id="chatMessages" class="flex-1 overflow-y-auto p-4 space-y-1 hidden"
                        style="background:#f9fafb;"></div>
                    <!-- Input -->
                    <div id="chatInputArea" class="hidden p-3 bg-white border-t border-gray-200">
                        <form id="messageForm" class="flex gap-2">
                            <input type="text" id="messageInput" placeholder="Nhập tin nhắn..."
                                class="flex-1 border border-gray-200 rounded-full px-4 py-2 text-sm focus:outline-none focus:border-primary transition">
                            <button type="submit"
                                class="bg-primary hover:bg-primary-dark text-white text-sm font-semibold px-4 py-2 rounded-full transition">
                                <i class="fas fa-paper-plane"></i>
                            </button>
                        </form>
                    </div>
                </div>
            </main>

            <script>
                var CTX = '${pageContext.request.contextPath}';
                var CURRENT_USER = {
                    username: '${sessionScope.username}',
                    fullName: '${sessionScope.fullName}'
                };
                var CHAT_WITH = '${param.chatWith}'; // from URL param ?chatWith=username
            </script>
            <script src="${pageContext.request.contextPath}/js/chat-servlet.js"></script>
        </body>

        </html>