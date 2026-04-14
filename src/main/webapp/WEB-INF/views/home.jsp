<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="vi">

        <c:set var="pageTitle" value="PTIT Social - Trang Chủ" />
        <jsp:include page="/WEB-INF/views/layout/header.jsp" />

        <body class="bg-gray-100 font-sans text-gray-900" data-username="<c:out value='${sessionScope.username}'/>"
            data-fullname="<c:out value='${sessionScope.fullName}'/>"
            data-avatar="<c:out value='${sessionScope.avatar}'/>" data-role="<c:out value='${sessionScope.role}'/>"
            data-ctx="<c:out value='${pageContext.request.contextPath}'/>"
            data-csrftoken="<c:out value='${sessionScope.csrfToken}'/>">

            <!-- NAVBAR -->
            <jsp:include page="/WEB-INF/views/layout/navbar.jsp">
                <jsp:param name="activeMenu" value="home" />
            </jsp:include>

            <!-- PAGE CONTENT — 2 column layout -->
            <main class="pt-20 pb-10">
                <div class="max-w-6xl mx-auto px-4 flex gap-5">

                    <!-- ═══════════════════════════════════════════ -->
                    <!--  LEFT: Main Feed                           -->
                    <!-- ═══════════════════════════════════════════ -->
                    <div class="flex-1 min-w-0">

                        <!-- Search Bar -->
                        <div class="bg-white rounded-2xl shadow-sm mb-4 p-3 flex items-center gap-3">
                            <i class="fas fa-search text-gray-400 ml-1"></i>
                            <input type="text" id="searchInput" placeholder="Tìm kiếm bài viết, tác giả, #chủ-đề..."
                                class="flex-1 bg-transparent text-sm text-gray-800 placeholder-gray-400 focus:outline-none"
                                onkeyup="if(event.key==='Enter') searchPosts()">
                            <button onclick="searchPosts()"
                                class="bg-primary hover:bg-primary-dark text-white text-xs font-semibold px-4 py-2 rounded-full transition">
                                Tìm
                            </button>
                            <button id="clearSearchBtn" onclick="clearSearch()"
                                class="hidden text-xs text-gray-400 hover:text-red-500 transition px-2 py-1">
                                <i class="fas fa-times"></i> Xóa
                            </button>
                        </div>

                        <!-- Create Post -->
                        <div class="bg-white rounded-2xl shadow-sm mb-4 p-5">
                            <form id="postForm">
                                <textarea id="postContent" rows="3"
                                    class="w-full bg-white border border-gray-200 rounded-xl px-4 py-3 text-sm text-gray-800 placeholder-gray-400 resize-none focus:outline-none focus:border-primary focus:bg-white transition"
                                    placeholder="Bạn đang nghĩ gì? Dùng #chủ-đề để gắn thẻ bài viết..."></textarea>

                                <!-- Hashtag quick-insert buttons -->
                                <div class="flex flex-wrap gap-1.5 mt-2" id="quickTags">
                                    <button type="button" onclick="insertTag('#just-for-fun')"
                                        class="text-xs bg-blue-50 text-blue-600 hover:bg-blue-100 px-2.5 py-1 rounded-full transition font-medium">#just-for-fun</button>
                                    <button type="button" onclick="insertTag('#quan-trọng')"
                                        class="text-xs bg-red-50 text-red-500 hover:bg-red-100 px-2.5 py-1 rounded-full transition font-medium">#quan-trọng</button>
                                    <button type="button" onclick="insertTag('#hỏi-đáp')"
                                        class="text-xs bg-green-50 text-green-600 hover:bg-green-100 px-2.5 py-1 rounded-full transition font-medium">#hỏi-đáp</button>
                                    <button type="button" onclick="insertTag('#chia-sẻ')"
                                        class="text-xs bg-purple-50 text-purple-600 hover:bg-purple-100 px-2.5 py-1 rounded-full transition font-medium">#chia-sẻ</button>
                                    <button type="button" onclick="insertTag('#học-tập')"
                                        class="text-xs bg-yellow-50 text-yellow-700 hover:bg-yellow-100 px-2.5 py-1 rounded-full transition font-medium">#học-tập</button>
                                </div>

                                <!-- Image Upload Preview -->
                                <div id="createPostImagePreviewContainer" class="hidden mt-3 relative inline-block">
                                    <img id="createPostImagePreview" src=""
                                        class="h-32 rounded-lg object-cover border border-gray-200 shadow-sm">
                                    <button type="button" id="cancelPostImagePreview"
                                        class="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center text-xs shadow hover:bg-red-600 transition">
                                        <i class="fas fa-times"></i>
                                    </button>
                                </div>
                                <input type="file" id="postImageFile" accept="image/*" class="hidden">

                                <div class="flex items-center justify-between mt-3">
                                    <div class="flex items-center gap-2 text-gray-500 hover:text-primary transition cursor-pointer text-sm px-2 font-medium"
                                        onclick="document.getElementById('postImageFile').click()">
                                        <i class="fas fa-image text-lg"></i><span>Hình ảnh</span>
                                    </div>
                                    <button type="submit" id="postSubmitBtn"
                                        class="flex items-center gap-2 bg-primary hover:bg-primary-dark text-white text-sm font-semibold px-5 py-2 rounded-full transition shadow-sm disabled:opacity-50">
                                        <i class="fas fa-paper-plane"></i> Đăng
                                    </button>
                                </div>
                            </form>
                        </div>

                        <!-- Search result indicator -->
                        <div id="searchIndicator"
                            class="hidden bg-blue-50 border border-blue-200 rounded-2xl p-3 mb-4 flex items-center justify-between">
                            <span class="text-sm text-blue-700"><i class="fas fa-search mr-1"></i>Kết quả tìm kiếm cho:
                                <strong id="searchQueryText"></strong></span>
                            <button onclick="clearSearch()"
                                class="text-xs text-blue-500 hover:text-blue-700 font-medium"><i
                                    class="fas fa-times mr-1"></i>Xóa bộ lọc</button>
                        </div>

                        <!-- Posts Feed -->
                        <div id="postsFeed"></div>
                    </div>

                    <!-- ═══════════════════════════════════════════ -->
                    <!--  RIGHT: Sidebar                            -->
                    <!-- ═══════════════════════════════════════════ -->
                    <div class="w-80 flex-shrink-0 space-y-4 hidden lg:block">

                        <!-- Announcements Box -->
                        <div class="bg-white rounded-2xl shadow-sm overflow-hidden">
                            <div class="bg-gradient-to-r from-primary to-red-700 px-4 py-3 flex items-center gap-2">
                                <i class="fas fa-bullhorn text-white text-sm"></i>
                                <h3 class="text-white font-semibold text-sm">Thông báo</h3>
                            </div>
                            <div id="announcementsBox" class="p-3 space-y-0 max-h-80 overflow-y-auto">
                                <div class="text-xs text-gray-400 text-center py-4">Đang tải...</div>
                            </div>
                        </div>

                        <!-- Topics Box -->
                        <div class="bg-white rounded-2xl shadow-sm overflow-hidden">
                            <div class="bg-gradient-to-r from-blue-600 to-indigo-600 px-4 py-3 flex items-center gap-2">
                                <i class="fas fa-hashtag text-white text-sm"></i>
                                <h3 class="text-white font-semibold text-sm">Chủ đề nổi bật</h3>
                            </div>
                            <div id="topicsBox" class="p-3 flex flex-wrap gap-1.5">
                                <div class="text-xs text-gray-400 text-center py-4 w-full">Đang tải...</div>
                            </div>
                        </div>

                    </div>
                </div>
            </main>

            <!-- FAB Chat -->
            <a href="${pageContext.request.contextPath}/ChatServlet"
                class="fixed bottom-7 right-7 bg-primary hover:bg-primary-dark text-white rounded-full flex items-center justify-center text-xl shadow-lg transition z-40"
                style="width:52px;height:52px">
                <i class="fas fa-comment-dots"></i>
            </a>

            <script>
                // Session info from server is passed via dataset attributes on body

                function toggleUserMenu() {
                    document.getElementById('userMenu').classList.toggle('hidden');
                }
                document.addEventListener('click', function (e) {
                    var wrap = document.getElementById('navAvatarWrap');
                    if (wrap && !wrap.contains(e.target)) {
                        document.getElementById('userMenu').classList.add('hidden');
                    }
                });

                // Insert hashtag into textarea
                function insertTag(tag) {
                    var ta = document.getElementById('postContent');
                    var val = ta.value;
                    // Add space before tag if needed
                    if (val.length > 0 && val[val.length - 1] !== ' ' && val[val.length - 1] !== '\n') {
                        val += ' ';
                    }
                    ta.value = val + tag + ' ';
                    ta.focus();
                }
            </script>
            <script src="${pageContext.request.contextPath}/js/api-client.js?v=3.1"></script>
            <script src="${pageContext.request.contextPath}/js/home-servlet.js?v=3.1"></script>
        </body>

        </html>
