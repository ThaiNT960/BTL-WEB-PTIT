<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="vi">

        <c:set var="pageTitle" value="PTIT Social - Trang Chủ" />
        <jsp:include page="/WEB-INF/views/layout/header.jsp" />

        <body class="bg-gray-100 font-sans text-gray-900">

            <!-- NAVBAR -->
            <jsp:include page="/WEB-INF/views/layout/navbar.jsp">
                <jsp:param name="activeMenu" value="home" />
            </jsp:include>

            <!-- PAGE CONTENT -->
            <main class="pt-20 pb-10">
                <div class="max-w-3xl mx-auto px-4">

                    <!-- Create Post -->
                    <div class="bg-white rounded-2xl shadow-sm mb-4 p-5">
                        <form id="postForm">
                            <textarea id="postContent" rows="3"
                                class="w-full bg-white border border-gray-200 rounded-xl px-4 py-3 text-sm text-gray-800 placeholder-gray-400 resize-none focus:outline-none focus:border-primary focus:bg-white transition"
                                placeholder="Bạn đang nghĩ gì?"></textarea>
                                
                            <!-- Image Upload Preview -->
                            <div id="createPostImagePreviewContainer" class="hidden mt-3 relative inline-block">
                                <img id="createPostImagePreview" src="" class="h-32 rounded-lg object-cover border border-gray-200 shadow-sm">
                                <button type="button" id="cancelPostImagePreview" class="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center text-xs shadow hover:bg-red-600 transition">
                                    <i class="fas fa-times"></i>
                                </button>
                            </div>
                            <input type="file" id="postImageFile" accept="image/*" class="hidden">

                            <div class="flex items-center justify-between mt-3">
                                <div class="flex items-center gap-2 text-gray-500 hover:text-primary transition cursor-pointer text-sm px-2 font-medium" onclick="document.getElementById('postImageFile').click()">
                                    <i class="fas fa-image text-lg"></i><span>Hình ảnh</span>
                                </div>
                                <button type="submit" id="postSubmitBtn"
                                    class="flex items-center gap-2 bg-primary hover:bg-primary-dark text-white text-sm font-semibold px-5 py-2 rounded-full transition shadow-sm disabled:opacity-50">
                                    <i class="fas fa-paper-plane"></i> Đăng
                                </button>
                            </div>
                        </form>
                    </div>

                    <!-- Posts Feed -->
                    <div id="postsFeed"></div>
                </div>
            </main>

            <!-- FAB Chat -->
            <a href="${pageContext.request.contextPath}/ChatServlet"
                class="fixed bottom-7 right-7 bg-primary hover:bg-primary-dark text-white rounded-full flex items-center justify-center text-xl shadow-lg transition z-40"
                style="width:52px;height:52px">
                <i class="fas fa-comment-dots"></i>
            </a>

            <script>
                // Session info from server
                var CURRENT_USER = {
                    username: '${sessionScope.username}',
                    fullName: '${sessionScope.fullName}',
                    avatar: '${sessionScope.avatar}',
                    role: '${sessionScope.role}'
                };
                var CTX = '${pageContext.request.contextPath}';

                function toggleUserMenu() {
                    document.getElementById('userMenu').classList.toggle('hidden');
                }
                document.addEventListener('click', function (e) {
                    var wrap = document.getElementById('navAvatarWrap');
                    if (wrap && !wrap.contains(e.target)) {
                        document.getElementById('userMenu').classList.add('hidden');
                    }
                });
            </script>
            <script src="${pageContext.request.contextPath}/js/api-client.js?v=2.0"></script>
            <script src="${pageContext.request.contextPath}/js/home-servlet.js?v=2.0"></script>
        </body>

        </html>