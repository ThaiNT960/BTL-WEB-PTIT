<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<nav class="fixed top-0 left-0 right-0 z-50 bg-white border-b border-gray-200 shadow-sm" style="height:60px">
    <div class="max-w-6xl mx-auto px-5 h-full flex items-center justify-between gap-4">
        <a href="${pageContext.request.contextPath}/HomeServlet" class="flex items-center gap-2 text-primary font-bold text-lg flex-shrink-0 no-underline">
            <img src="${pageContext.request.contextPath}/img/logo_ptit.svg" alt="PTIT Logo" class="w-10 h-10 object-contain">
            PTIT Social
        </a>
        <div class="flex items-center gap-1">
            <a href="${pageContext.request.contextPath}/HomeServlet" class="nav-link ${param.activeMenu == 'home' ? 'active' : ''}"><i class="fas fa-home"></i> Trang chủ</a>
            <a href="${pageContext.request.contextPath}/FriendServlet" class="relative nav-link ${param.activeMenu == 'friend' ? 'active' : ''}">
                <i class="fas fa-user-friends"></i> Bạn bè
                <span id="globalFriendBadge" class="hidden absolute top-0 -right-2 bg-red-500 text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full shadow-sm leading-none border border-white"></span>
            </a>
            <a href="${pageContext.request.contextPath}/ChatServlet" class="relative nav-link ${param.activeMenu == 'chat' ? 'active' : ''}">
                <i class="fas fa-comment-dots"></i> Tin nhắn
                <span id="globalMessageBadge" class="hidden absolute top-0 -right-2 bg-red-500 text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full shadow-sm leading-none border border-white"></span>
            </a>
        </div>
        <div class="flex items-center gap-3 flex-shrink-0 relative">
            <c:if test="${sessionScope.role == 'ROLE_ADMIN'}">
                <a href="${pageContext.request.contextPath}/AdminServlet" class="nav-link text-primary font-semibold"><i class="fas fa-shield-alt"></i> Admin</a>
            </c:if>
            <div class="relative" id="navAvatarWrap">
                <button onclick="document.getElementById('userMenu').classList.toggle('hidden')" class="w-10 h-10 bg-primary rounded-full flex items-center justify-center text-white font-bold text-base focus:outline-none" id="navAvatar">
                    <c:choose>
                        <c:when test="${not empty sessionScope.avatar}">
                            <img src="${sessionScope.avatar}" class="w-full h-full object-cover rounded-full" onerror="this.parentElement.textContent='${sessionScope.fullName != null ? sessionScope.fullName.charAt(0) : 'U'}'" />
                        </c:when>
                        <c:otherwise>
                            ${empty sessionScope.fullName ? 'U' : sessionScope.fullName.substring(0,1).toUpperCase()}
                        </c:otherwise>
                    </c:choose>
                </button>
                <div id="userMenu" class="hidden absolute right-0 top-12 bg-white border border-gray-200 rounded-xl shadow-lg w-44 overflow-hidden z-50">
                    <a href="${pageContext.request.contextPath}/ProfileServlet" class="flex items-center gap-2 px-4 py-3 text-sm text-gray-700 hover:bg-gray-50"><i class="fas fa-user text-primary w-4"></i> Trang cá nhân</a>
                    <a href="${pageContext.request.contextPath}/LogoutServlet" class="flex items-center gap-2 px-4 py-3 text-sm text-gray-700 hover:bg-gray-50"><i class="fas fa-sign-out-alt text-primary w-4"></i> Đăng xuất</a>
                </div>
            </div>
        </div>
    </div>
</nav>
