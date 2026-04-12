<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="vi">

        <c:set var="pageTitle" value="PTIT Social - Bạn Bè" />
        <jsp:include page="/WEB-INF/views/layout/header.jsp" />

        <body class="bg-gray-100 font-sans text-gray-900"
              data-username="<c:out value='${sessionScope.username}'/>"
              data-fullname="<c:out value='${sessionScope.fullName}'/>"
              data-avatar="<c:out value='${sessionScope.avatar}'/>"
              data-role="<c:out value='${sessionScope.role}'/>"
              data-ctx="<c:out value='${pageContext.request.contextPath}'/>"
              data-csrftoken="<c:out value='${sessionScope.csrfToken}'/>">

            <jsp:include page="/WEB-INF/views/layout/navbar.jsp">
                <jsp:param name="activeMenu" value="friend" />
            </jsp:include>

            <main class="pt-20 pb-10">
                <div class="max-w-3xl mx-auto px-4 space-y-4">

                    <div class="bg-white rounded-2xl shadow-sm p-5">
                        <h2 class="font-semibold text-gray-900 text-sm mb-3">
                            <i class="fas fa-search text-primary mr-2"></i>Tìm kiếm bạn bè
                        </h2>
                        <form id="searchForm" onsubmit="event.preventDefault(); searchFriend();" class="flex gap-2">
                            <input type="text" id="searchInput" placeholder="Nhập tên hoặc username..."
                                class="flex-1 border border-gray-200 rounded-full px-4 py-2 text-sm focus:outline-none focus:border-primary transition"
                                required>
                            <button type="submit"
                                class="bg-primary hover:bg-primary-dark text-white text-sm font-semibold px-4 py-2 rounded-full transition">Tìm</button>
                        </form>
                        <div id="searchResults" class="mt-3"></div>
                    </div>

                    <div class="bg-white rounded-2xl shadow-sm p-5">
                        <h2 class="font-semibold text-gray-900 text-sm mb-3">
                            <i class="fas fa-user-clock text-primary mr-2"></i>Lời mời kết bạn
                        </h2>
                        <div id="friendRequests">
                            <c:choose>
                                <c:when test="${empty friendRequests}">
                                    <p class="text-center text-gray-400 text-sm py-6">Không có lời mời kết bạn nào</p>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="req" items="${friendRequests}">
                                        <div
                                            class="flex items-center gap-3 py-3 border-b border-gray-100 last:border-0">

                                            <a href="${pageContext.request.contextPath}/ProfileServlet?username=${req.sender.username}"
                                                class="flex-shrink-0 hover:opacity-80 transition">
                                                <c:choose>
                                                    <c:when test="${not empty req.sender.avatar}">
                                                        <img src="${req.sender.avatar}" class="w-11 h-11 rounded-full object-cover" alt="avatar">
                                                    </c:when>
                                                    <c:otherwise>
                                                        <div class="w-11 h-11 rounded-full bg-primary flex items-center justify-center text-white font-bold">
                                                            <c:out value="${empty req.sender.fullName ? req.sender.username.substring(0,1).toUpperCase() : req.sender.fullName.substring(0,1).toUpperCase()}" />
                                                        </div>
                                                    </c:otherwise>
                                                </c:choose>
                                            </a>

                                            <div class="flex-1 min-w-0">
                                                <a href="${pageContext.request.contextPath}/ProfileServlet?username=${req.sender.username}"
                                                    class="hover:underline">
                                                    <p class="font-semibold text-sm text-gray-900">
                                                        <c:out value="${req.sender.fullName}" /></p>
                                                </a>
                                            </div>

                                            <div class="flex gap-2">
                                                <button data-req-id="${req.id}" data-action="accept"
                                                    class="js-friend-action text-xs bg-primary hover:bg-primary-dark text-white font-semibold px-3 py-1.5 rounded-full transition">Chấp nhận</button>
                                                <button data-req-id="${req.id}" data-action="reject"
                                                    class="js-friend-action text-xs bg-gray-100 hover:bg-gray-200 text-gray-600 font-medium px-3 py-1.5 rounded-full transition">Từ chối</button>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <div class="bg-white rounded-2xl shadow-sm p-5">
                        <h2 class="font-semibold text-gray-900 text-sm mb-3">
                            <i class="fas fa-user-friends text-primary mr-2"></i>Bạn bè (
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

                                            <a href="${pageContext.request.contextPath}/ProfileServlet?username=${f.username}"
                                                class="flex-shrink-0 hover:opacity-80 transition">
                                                <c:choose>
                                                    <c:when test="${not empty f.avatar}">
                                                        <img src="${f.avatar}" class="w-11 h-11 rounded-full object-cover" alt="avatar">
                                                    </c:when>
                                                    <c:otherwise>
                                                        <div class="w-11 h-11 rounded-full bg-primary flex items-center justify-center text-white font-bold">
                                                            <c:out value="${empty f.fullName ? f.username.substring(0,1).toUpperCase() : f.fullName.substring(0,1).toUpperCase()}" />
                                                        </div>
                                                    </c:otherwise>
                                                </c:choose>
                                            </a>

                                            <div class="flex-1 min-w-0">
                                                <a href="${pageContext.request.contextPath}/ProfileServlet?username=${f.username}"
                                                    class="hover:underline">
                                                    <p class="font-semibold text-sm text-gray-900"><c:out value="${f.fullName}" /></p>
                                                </a>
                                            </div>

                                            <div class="flex gap-2">
                                                <a href="${pageContext.request.contextPath}/ChatServlet?chatWith=${f.username}"
                                                    class="text-xs bg-gray-100 hover:bg-gray-200 text-gray-600 font-medium px-3 py-1.5 rounded-full transition">
                                                    <i class="fas fa-comment-dots"></i> Nhắn tin
                                                </a>
                                                <button data-username="${f.username}" data-action="unfriend"
                                                    class="js-friend-action text-xs bg-red-50 hover:bg-red-100 text-red-600 font-medium px-3 py-1.5 rounded-full transition">
                                                    Hủy kết bạn
                                                </button>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </main>

            <script>
                // Read from body dataset
            </script>
            <script src="${pageContext.request.contextPath}/js/api-client.js"></script>
            <script src="${pageContext.request.contextPath}/js/friend-servlet.js"></script>
        </body>

        </html>
