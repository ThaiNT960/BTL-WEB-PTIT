<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="vi">

        <c:set var="pageTitle" value="PTIT Social - Tin Nhắn" />
        <jsp:include page="/WEB-INF/views/layout/header.jsp" />

        <style>
            .message-bubble {
                max-width: 75%;
                position: relative;
                transition: all 0.2s ease;
            }

            .message-bubble:hover {
                filter: brightness(0.98);
            }

            .msg-sent {
                background: linear-gradient(135deg, #FF4B2B, #FF416C);
                color: white;
                border-bottom-right-radius: 4px;
                box-shadow: 0 4px 15px rgba(255, 75, 43, 0.2);
            }

            .msg-received {
                background: white;
                color: #1f2937;
                border-bottom-left-radius: 4px;
                box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
            }

            .chat-contact-item.active {
                background-color: #fce7e7;
                border-right: 4px solid #FF4B2B;
            }
        </style>

        <body class="bg-gray-100 font-sans text-gray-900"
              data-username="<c:out value='${sessionScope.username}'/>"
              data-fullname="<c:out value='${sessionScope.fullName}'/>"
              data-avatar="<c:out value='${sessionScope.avatar}'/>"
              data-role="<c:out value='${sessionScope.role}'/>"
              data-ctx="<c:out value='${pageContext.request.contextPath}'/>"
              data-csrftoken="<c:out value='${sessionScope.csrfToken}'/>">

            <!-- NAVBAR -->
            <jsp:include page="/WEB-INF/views/layout/navbar.jsp">
                <jsp:param name="activeMenu" value="chat" />
            </jsp:include>

            <!-- CHAT LAYOUT -->
            <main class="pt-20 pb-10">
                <div class="max-w-7xl mx-auto px-4">
                    <div class="bg-white rounded-2xl shadow-sm flex overflow-hidden"
                        style="height: calc(100vh - 120px);">
                        <!-- Friends Sidebar -->
                        <div class="w-80 flex-shrink-0 border-r border-gray-200 flex flex-col" style="height: 100%;">
                            <div class="p-4 border-b border-gray-100">
                                <h2 class="font-semibold text-gray-900 text-sm">Tin nhắn</h2>
                                <div class="mt-3 relative">
                                    <i class="fas fa-search absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-sm"></i>
                                    <input type="text" id="friendSearchInput" placeholder="Tìm kiếm vòng kết nối..." class="w-full bg-gray-50 border border-gray-200 text-sm rounded-full pl-9 pr-4 py-2 focus:outline-none focus:border-primary focus:bg-white transition-colors">
                                </div>
                            </div>
                            <div id="friendsList" class="flex-1 overflow-y-auto p-2">
                                <c:choose>
                                    <c:when test="${empty friends}">
                                        <p class="text-center text-gray-400 text-sm py-8">Chưa có bạn bè.<br>Hãy thêm
                                            bạn bè
                                            trước!</p>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="f" items="${friends}">
                                            <div class="chat-contact-item flex items-center gap-3 p-3 rounded-xl cursor-pointer hover:bg-gray-50 transition"
                                                data-username="${f.username}" data-fullname="${f.fullName}" data-avatar="${f.avatar != null ? f.avatar : ''}"
                                                onclick="selectChat('${f.username}', '${f.fullName}', '${f.avatar != null ? f.avatar : ''}')">
                                                <div class="relative flex-shrink-0">
                                                    <c:choose>
                                                        <c:when test="${not empty f.avatar}">
                                                            <img src="${f.avatar}"
                                                                class="w-11 h-11 rounded-full object-cover border border-gray-100 shadow-sm"
                                                                onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                                                            <div
                                                                class="hidden w-11 h-11 rounded-full bg-primary flex items-center justify-center text-white font-bold text-sm shadow-sm">
                                                                ${empty f.fullName ?
                                                                f.username.substring(0,1).toUpperCase() :
                                                                f.fullName.substring(0,1).toUpperCase()}
                                                            </div>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <div
                                                                class="w-11 h-11 rounded-full bg-primary flex items-center justify-center text-white font-bold text-sm shadow-sm">
                                                                ${empty f.fullName ?
                                                                f.username.substring(0,1).toUpperCase() :
                                                                f.fullName.substring(0,1).toUpperCase()}
                                                            </div>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                                <div class="flex-1 min-w-0">
                                                    <p class="font-semibold text-sm text-gray-900 truncate">
                                                        ${f.fullName}</p>
                                                </div>
                                                <div class="flex-shrink-0">
                                                    <span class="unread-badge hidden bg-red-500 text-white text-xs font-bold px-2 py-0.5 rounded-full"></span>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <!-- Chat Window -->
                        <div class="flex-1 flex flex-col" style="height: 100%;">
                            <!-- Chat header -->
                            <div id="chatWindowHeader"
                                class="hidden px-5 py-3 bg-white border-b border-gray-200 flex items-center gap-3">
                                <a id="chatHeaderProfileLink" href="#" class="flex items-center gap-3 flex-1 hover:opacity-80 transition group">
                                    <div class="relative flex-shrink-0" id="chatHeaderAvatarContainer">
                                        <img id="chatHeaderAvatarImg" src="" class="hidden w-10 h-10 rounded-full object-cover border border-gray-100 shadow-sm" onerror="this.style.display='none'; document.getElementById('chatHeaderAvatarNoImg').style.display='flex';">
                                        <div id="chatHeaderAvatarNoImg" class="w-10 h-10 rounded-full bg-primary flex items-center justify-center text-white font-bold text-sm shadow-sm">
                                            ?
                                        </div>
                                    </div>
                                    <div>
                                        <p class="font-semibold text-sm text-gray-900 group-hover:underline" id="chatTitle">Chọn cuộc trò chuyện</p>
                                        <p class="text-xs text-gray-400">Xem trang cá nhân</p>
                                    </div>
                                </a>
                                <div class="flex items-center gap-2">
                                    <button onclick="clearHistory()" class="text-gray-400 hover:text-red-500 transition p-2" title="Xóa lịch sử trò chuyện">
                                        <i class="fas fa-trash-alt"></i>
                                    </button>
                                </div>
                            </div>
                            <div id="chatPlaceholder"
                                class="flex-1 flex items-center justify-center text-gray-400 text-sm">
                                <div class="text-center">
                                    <i class="fas fa-comment-dots text-4xl mb-3 opacity-30"></i>
                                    <p>Chọn một người bạn để bắt đầu trò chuyện</p>
                                </div>
                            </div>
                            <!-- Messages area -->
                            <div id="chatMessages" class="flex-1 overflow-y-auto p-4 space-y-4 hidden scroll-smooth"
                                style="background:#f0f2f5;">
                            </div>

                            <!-- Empty Message Placeholder (inside chatMessages but managed by JS) -->
                            <div id="emptyChatPlaceholder"
                                class="hidden flex-1 flex flex-col items-center justify-center text-gray-400 p-8">
                                <div class="bg-white p-6 rounded-full shadow-sm mb-4">
                                    <i class="fas fa-comments text-4xl text-primary opacity-20"></i>
                                </div>
                                <p class="font-medium">Chưa có tin nhắn nào</p>
                                <p class="text-xs mt-1">Hãy gửi lời chào để bắt đầu cuộc trò chuyện!</p>
                            </div>
                            <div id="notFriendPlaceholder" class="hidden p-4 bg-gray-50 border-t border-gray-200 text-center text-gray-500 text-sm">
                                Bạn phải là bạn bè mới có thể gửi tin nhắn.
                            </div>
                            <!-- Input -->
                            <div id="chatInputArea" class="hidden p-3 bg-white border-t border-gray-200">
                                <div id="imagePreviewContainer" class="hidden mb-2 relative inline-block">
                                    <img id="chatImagePreview" src=""
                                        class="h-20 rounded-lg object-cover border border-gray-200">
                                    <button id="cancelImagePreview"
                                        class="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center text-xs shadow hover:bg-red-600">
                                        <i class="fas fa-times"></i>
                                    </button>
                                </div>
                                <form id="messageForm" class="flex gap-2 items-center">
                                    <input type="file" id="chatImageInput" accept="image/*" class="hidden">
                                    <button type="button" id="chatAttachBtn"
                                        class="text-gray-400 hover:text-primary transition p-2 focus:outline-none">
                                        <i class="fas fa-paperclip text-lg"></i>
                                    </button>
                                    <input type="text" id="messageInput" placeholder="Nhập tin nhắn..."
                                        class="flex-1 border border-gray-200 rounded-full px-4 py-2 text-sm focus:outline-none focus:border-primary transition">
                                    <button type="submit"
                                        class="bg-primary hover:bg-primary-dark text-white text-sm font-semibold px-4 py-2 rounded-full transition">
                                        <i class="fas fa-paper-plane"></i>
                                    </button>
                                </form>
                            </div>
                        </div>
                    </div>
            </main>

            <script>
                // Variables are read from body dataset
                var CHAT_WITH = '<c:out value="${param.chatWith}"/>'; // from URL param ?chatWith=username
            </script>
            <script src="${pageContext.request.contextPath}/js/api-client.js?v=2.3"></script>
            <script src="${pageContext.request.contextPath}/js/chat-servlet.js?v=2.2"></script>
        </body>

        </html>
