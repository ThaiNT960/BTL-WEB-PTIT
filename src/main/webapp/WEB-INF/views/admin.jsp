<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="vi">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>PTIT Social - Quản Trị</title>
            <script src="https://cdn.tailwindcss.com"></script>
            <script>tailwind.config = { theme: { extend: { colors: { primary: '#ae1a21', 'primary-dark': '#8a141a' } } } }</script>
            <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
            <style>
                .mode-card {
                    transition: all 0.3s ease;
                    cursor: pointer;
                    border: 2px solid transparent;
                }

                .mode-card:hover {
                    transform: translateY(-2px);
                    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
                }

                .mode-card.active {
                    border-color: #ae1a21;
                    background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
                }

                .mode-card.active .mode-icon {
                    background: #ae1a21;
                    color: white;
                }

                .tab-btn {
                    transition: all 0.2s ease;
                }

                .tab-btn.active {
                    color: #ae1a21;
                    border-bottom: 3px solid #ae1a21;
                    font-weight: 600;
                }

                .status-badge {
                    font-size: 11px;
                    padding: 3px 10px;
                    border-radius: 9999px;
                    font-weight: 600;
                }

                .status-approved {
                    background: #dcfce7;
                    color: #166534;
                }

                .status-pending {
                    background: #fef9c3;
                    color: #854d0e;
                }

                .status-rejected {
                    background: #fee2e2;
                    color: #991b1b;
                }

                .ai-status-dot {
                    width: 8px;
                    height: 8px;
                    border-radius: 50%;
                    display: inline-block;
                }

                .ai-online {
                    background: #22c55e;
                    box-shadow: 0 0 6px #22c55e;
                }

                .ai-offline {
                    background: #ef4444;
                    box-shadow: 0 0 6px #ef4444;
                }

                .fade-in {
                    animation: fadeIn 0.3s ease;
                }

                @keyframes fadeIn {
                    from {
                        opacity: 0;
                        transform: translateY(10px);
                    }

                    to {
                        opacity: 1;
                        transform: translateY(0);
                    }
                }

                .pulse-glow {
                    animation: pulseGlow 2s infinite;
                }

                @keyframes pulseGlow {

                    0%,
                    100% {
                        box-shadow: 0 0 5px rgba(174, 26, 33, 0.3);
                    }

                    50% {
                        box-shadow: 0 0 15px rgba(174, 26, 33, 0.6);
                    }
                }
            </style>
        </head>

        <body class="bg-gray-100 font-sans text-gray-900" data-username="<c:out value='${sessionScope.username}'/>"
            data-fullname="<c:out value='${sessionScope.fullName}'/>"
            data-avatar="<c:out value='${sessionScope.avatar}'/>" data-role="<c:out value='${sessionScope.role}'/>"
            data-ctx="<c:out value='${pageContext.request.contextPath}'/>"
            data-csrftoken="<c:out value='${sessionScope.csrfToken}'/>">

            <!-- NAVBAR -->
            <nav class="fixed top-0 left-0 right-0 z-50 bg-white border-b border-gray-200 shadow-sm"
                style="height:60px">
                <div class="max-w-6xl mx-auto px-5 h-full flex items-center justify-between gap-4">
                    <a href="${pageContext.request.contextPath}/HomeServlet"
                        class="flex items-center gap-2 text-primary font-bold text-lg">
                        <img src="${pageContext.request.contextPath}/img/logo_ptit.svg" alt="PTIT Logo"
                            class="w-10 h-10 object-contain"> PTIT Social
                    </a>
                    <span class="font-semibold text-primary text-sm"><i class="fas fa-shield-alt mr-1"></i>Bảng Quản
                        Trị</span>
                    <div class="flex items-center gap-3">
                        <a href="${pageContext.request.contextPath}/LogoutServlet"
                            class="text-sm text-gray-600 hover:text-primary"><i
                                class="fas fa-sign-out-alt mr-1"></i>Đăng xuất</a>
                    </div>
                </div>
            </nav>

            <main class="pt-20 pb-10">
                <div class="max-w-6xl mx-auto px-4 space-y-6">

                    <!-- ═══════════════════════════════════════════════════ -->
                    <!--  MODERATION SETTINGS CARD                         -->
                    <!-- ═══════════════════════════════════════════════════ -->
                    <div class="bg-white rounded-2xl shadow-sm p-5 fade-in">
                        <div class="flex items-center justify-between mb-4">
                            <h2 class="font-semibold text-gray-900">
                                <i class="fas fa-robot text-primary mr-2"></i>Chế độ Kiểm duyệt Bài viết
                            </h2>
                            <div class="flex items-center gap-2 text-xs">
                                <span class="ai-status-dot ${aiServiceAvailable ? 'ai-online' : 'ai-offline'}"></span>
                                <span class="${aiServiceAvailable ? 'text-green-600' : 'text-red-500'} font-medium">
                                    AI Service: ${aiServiceAvailable ? 'Đang chạy' : 'Không khả dụng'}
                                </span>
                                <button onclick="checkAiService()"
                                    class="text-gray-400 hover:text-primary transition ml-1" title="Kiểm tra lại">
                                    <i class="fas fa-sync-alt"></i>
                                </button>
                            </div>
                        </div>

                        <div class="grid grid-cols-3 gap-4">
                            <!-- Mode: NONE -->
                            <div class="mode-card rounded-xl p-4 bg-gray-50 ${moderationMode == 'NONE' ? 'active' : ''}"
                                onclick="setModerationMode('NONE')" id="mode-NONE">
                                <div
                                    class="mode-icon w-10 h-10 rounded-lg flex items-center justify-center mb-3 ${moderationMode == 'NONE' ? 'bg-primary text-white' : 'bg-gray-200 text-gray-500'}">
                                    <i class="fas fa-unlock text-lg"></i>
                                </div>
                                <h3 class="font-semibold text-sm text-gray-900 mb-1">Không kiểm duyệt</h3>
                                <p class="text-xs text-gray-500">Bài viết hiển thị ngay khi đăng. Không qua bất kỳ bước
                                    kiểm tra nào.</p>
                            </div>

                            <!-- Mode: MANUAL -->
                            <div class="mode-card rounded-xl p-4 bg-gray-50 ${moderationMode == 'MANUAL' ? 'active' : ''}"
                                onclick="setModerationMode('MANUAL')" id="mode-MANUAL">
                                <div
                                    class="mode-icon w-10 h-10 rounded-lg flex items-center justify-center mb-3 ${moderationMode == 'MANUAL' ? 'bg-primary text-white' : 'bg-gray-200 text-gray-500'}">
                                    <i class="fas fa-user-shield text-lg"></i>
                                </div>
                                <h3 class="font-semibold text-sm text-gray-900 mb-1">Kiểm duyệt thủ công</h3>
                                <p class="text-xs text-gray-500">Mọi bài viết phải chờ Admin xét duyệt trước khi hiển
                                    thị.</p>
                            </div>

                            <!-- Mode: AUTO_AI -->
                            <div class="mode-card rounded-xl p-4 bg-gray-50 ${moderationMode == 'AUTO_AI' ? 'active' : ''}"
                                onclick="setModerationMode('AUTO_AI')" id="mode-AUTO_AI">
                                <div
                                    class="mode-icon w-10 h-10 rounded-lg flex items-center justify-center mb-3 ${moderationMode == 'AUTO_AI' ? 'bg-primary text-white' : 'bg-gray-200 text-gray-500'}">
                                    <i class="fas fa-brain text-lg"></i>
                                </div>
                                <h3 class="font-semibold text-sm text-gray-900 mb-1">Tự động (AI)</h3>
                                <p class="text-xs text-gray-500">AI PhoBERT tự động phân loại. Clean → duyệt, Toxic → từ
                                    chối.</p>
                            </div>
                        </div>
                    </div>

                    <!-- Stats Cards -->
                    <div class="grid grid-cols-4 gap-4">
                        <div class="bg-white rounded-2xl shadow-sm p-5 flex items-center gap-4">
                            <div class="w-12 h-12 bg-red-50 rounded-xl flex items-center justify-center">
                                <i class="fas fa-users text-primary text-xl"></i>
                            </div>
                            <div>
                                <p class="text-gray-500 text-sm">Người dùng</p>
                                <p class="font-bold text-2xl text-gray-900" id="statUsers">${users.size()}</p>
                            </div>
                        </div>
                        <div class="bg-white rounded-2xl shadow-sm p-5 flex items-center gap-4">
                            <div class="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center">
                                <i class="fas fa-file-alt text-blue-500 text-xl"></i>
                            </div>
                            <div>
                                <p class="text-gray-500 text-sm">Tổng bài viết</p>
                                <p class="font-bold text-2xl text-gray-900">${posts.size()}</p>
                            </div>
                        </div>
                        <div class="bg-white rounded-2xl shadow-sm p-5 flex items-center gap-4">
                            <div class="w-12 h-12 bg-yellow-50 rounded-xl flex items-center justify-center">
                                <i class="fas fa-hourglass-half text-yellow-500 text-xl"></i>
                            </div>
                            <div>
                                <p class="text-gray-500 text-sm">Chờ duyệt</p>
                                <p class="font-bold text-2xl text-yellow-600" id="statPending">${pendingCount}</p>
                            </div>
                        </div>
                        <div class="bg-white rounded-2xl shadow-sm p-5 flex items-center gap-4">
                            <div class="w-12 h-12 bg-red-50 rounded-xl flex items-center justify-center">
                                <i class="fas fa-ban text-red-500 text-xl"></i>
                            </div>
                            <div>
                                <p class="text-gray-500 text-sm">Bị từ chối</p>
                                <p class="font-bold text-2xl text-red-600" id="statRejected">${rejectedCount}</p>
                            </div>
                        </div>
                    </div>

                    <!-- Users Table -->
                    <div class="bg-white rounded-2xl shadow-sm p-5">
                        <div class="flex items-center justify-between mb-4">
                            <h2 class="font-semibold text-gray-900"><i class="fas fa-users text-primary mr-2"></i>Danh
                                sách người dùng</h2>
                            <button onclick="document.getElementById('addUserModal').classList.remove('hidden')"
                                class="text-xs bg-primary hover:bg-primary-dark text-white font-semibold px-4 py-2 rounded-full transition">
                                <i class="fas fa-plus mr-1"></i>Thêm user
                            </button>
                        </div>
                        <div class="overflow-x-auto">
                            <table class="w-full">
                                <thead>
                                    <tr class="border-b border-gray-100">
                                        <th class="text-left text-xs font-semibold text-gray-400 pb-3 pr-4">ID</th>
                                        <th class="text-left text-xs font-semibold text-gray-400 pb-3 pr-4">Tên</th>
                                        <th class="text-left text-xs font-semibold text-gray-400 pb-3 pr-4">Username
                                        </th>
                                        <th class="text-left text-xs font-semibold text-gray-400 pb-3 pr-4">Vai trò</th>
                                        <th class="text-left text-xs font-semibold text-gray-400 pb-3">Hành động</th>
                                    </tr>
                                </thead>
                                <tbody id="usersTableBody">
                                    <c:forEach var="u" items="${users}">
                                        <tr class="border-b border-gray-50 hover:bg-gray-50" id="user-row-${u.id}">
                                            <td class="py-3 pr-4 text-sm text-gray-500">${u.id}</td>
                                            <td class="py-3 pr-4">
                                                <div class="flex items-center gap-2">
                                                    <div
                                                        class="w-8 h-8 bg-primary rounded-full flex items-center justify-center text-white text-xs font-bold flex-shrink-0">
                                                        ${empty u.fullName ? u.username.substring(0,1).toUpperCase() :
                                                        u.fullName.substring(0,1).toUpperCase()}
                                                    </div>
                                                    <span class="text-sm font-medium text-gray-900">${u.fullName}</span>
                                                </div>
                                            </td>
                                            <td class="py-3 pr-4 text-sm text-gray-600">@${u.username}</td>
                                            <td class="py-3 pr-4">
                                                <span
                                                    class="text-xs font-semibold px-2.5 py-1 rounded-full ${u.role == 'ROLE_ADMIN' ? 'bg-red-50 text-primary' : 'bg-gray-100 text-gray-500'}">
                                                    ${u.role == 'ROLE_ADMIN' ? 'Admin' : 'User'}
                                                </span>
                                            </td>
                                            <td class="py-3">
                                                <c:choose>
                                                    <c:when test="${u.id == currentAdminId}">
                                                        <span class="text-xs text-gray-300 italic px-2 py-1">Bạn</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <button onclick="deleteUser(${u.id})"
                                                            class="text-xs text-gray-400 hover:text-red-500 transition px-2 py-1 rounded-lg hover:bg-red-50">
                                                            <i class="fas fa-trash"></i> Xóa
                                                        </button>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- ═══════════════════════════════════════════════════ -->
                    <!--  POSTS TABLE WITH MODERATION TABS                 -->
                    <!-- ═══════════════════════════════════════════════════ -->
                    <div class="bg-white rounded-2xl shadow-sm p-5 mt-6">
                        <div class="flex items-center justify-between mb-4">
                            <h2 class="font-semibold text-gray-900"><i
                                    class="fas fa-file-alt text-primary mr-2"></i>Quản lý Bài viết</h2>
                            <div class="flex items-center gap-2" id="bulkActions" style="display:none">
                                <button onclick="approveAllPending()"
                                    class="text-xs bg-green-500 hover:bg-green-600 text-white font-semibold px-4 py-2 rounded-full transition">
                                    <i class="fas fa-check-double mr-1"></i>Duyệt tất cả
                                </button>
                            </div>
                        </div>

                        <!-- Tabs -->
                        <div class="flex border-b border-gray-100 mb-4 gap-1">
                            <button class="tab-btn active px-4 py-2.5 text-sm text-gray-500"
                                onclick="switchTab('all', this)">
                                Tất cả <span
                                    class="ml-1 text-xs bg-gray-100 px-2 py-0.5 rounded-full">${posts.size()}</span>
                            </button>
                            <button class="tab-btn px-4 py-2.5 text-sm text-gray-500"
                                onclick="switchTab('pending', this)">
                                <i class="fas fa-hourglass-half text-yellow-500 mr-1"></i>Chờ duyệt
                                <span class="ml-1 text-xs bg-yellow-100 text-yellow-700 px-2 py-0.5 rounded-full"
                                    id="tabPendingCount">${pendingCount}</span>
                            </button>
                            <button class="tab-btn px-4 py-2.5 text-sm text-gray-500"
                                onclick="switchTab('rejected', this)">
                                <i class="fas fa-ban text-red-500 mr-1"></i>Bị từ chối
                                <span
                                    class="ml-1 text-xs bg-red-100 text-red-700 px-2 py-0.5 rounded-full">${rejectedCount}</span>
                            </button>
                            <button class="tab-btn px-4 py-2.5 text-sm text-gray-500"
                                onclick="switchTab('approved', this)">
                                <i class="fas fa-check-circle text-green-500 mr-1"></i>Đã duyệt
                            </button>
                        </div>

                        <div class="overflow-x-auto">
                            <table class="w-full">
                                <thead>
                                    <tr class="border-b border-gray-100">
                                        <th class="text-left text-xs font-semibold text-gray-400 pb-3 pr-4">ID</th>
                                        <th class="text-left text-xs font-semibold text-gray-400 pb-3 pr-4">Tác giả</th>
                                        <th class="text-left text-xs font-semibold text-gray-400 pb-3 pr-4">Nội dung
                                        </th>
                                        <th class="text-left text-xs font-semibold text-gray-400 pb-3 pr-4">Trạng thái
                                        </th>
                                        <th class="text-left text-xs font-semibold text-gray-400 pb-3 pr-4">AI Label
                                        </th>
                                        <th class="text-left text-xs font-semibold text-gray-400 pb-3 pr-4">Thời gian
                                        </th>
                                        <th class="text-left text-xs font-semibold text-gray-400 pb-3">Hành động</th>
                                    </tr>
                                </thead>
                                <tbody id="postsTableBody">
                                    <c:forEach var="p" items="${posts}">
                                        <tr class="border-b border-gray-50 hover:bg-gray-50 post-row"
                                            id="post-row-${p.id}"
                                            data-status="${empty p.status ? 'APPROVED' : p.status}">
                                            <td class="py-3 pr-4 text-sm text-gray-500">${p.id}</td>
                                            <td class="py-3 pr-4 text-sm font-medium text-gray-900">${p.user.fullName}
                                            </td>
                                            <td class="py-3 pr-4 text-sm text-gray-600 max-w-xs truncate">${p.content}
                                            </td>
                                            <td class="py-3 pr-4">
                                                <c:choose>
                                                    <c:when test="${p.status == 'PENDING'}">
                                                        <span class="status-badge status-pending"><i
                                                                class="fas fa-hourglass-half mr-1"></i>Chờ duyệt</span>
                                                    </c:when>
                                                    <c:when test="${p.status == 'REJECTED'}">
                                                        <span class="status-badge status-rejected"><i
                                                                class="fas fa-ban mr-1"></i>Từ chối</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="status-badge status-approved"><i
                                                                class="fas fa-check-circle mr-1"></i>Đã duyệt</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="py-3 pr-4 text-sm">
                                                <c:if test="${not empty p.moderationLabel}">
                                                    <c:choose>
                                                        <c:when test="${p.moderationLabel == 'CLEAN'}">
                                                            <span class="text-green-600 font-medium text-xs"><i
                                                                    class="fas fa-leaf mr-1"></i>Clean</span>
                                                        </c:when>
                                                        <c:when test="${p.moderationLabel == 'OFFENSIVE'}">
                                                            <span class="text-orange-500 font-medium text-xs"><i
                                                                    class="fas fa-exclamation-triangle mr-1"></i>Offensive</span>
                                                        </c:when>
                                                        <c:when test="${p.moderationLabel == 'HATE'}">
                                                            <span class="text-red-600 font-medium text-xs"><i
                                                                    class="fas fa-skull-crossbones mr-1"></i>Hate</span>
                                                        </c:when>
                                                    </c:choose>
                                                    <c:if test="${not empty p.moderationConfidence}">
                                                        <span class="text-gray-400 text-xs ml-1">${String.format("%.0f",
                                                            p.moderationConfidence * 100)}%</span>
                                                    </c:if>
                                                </c:if>
                                                <c:if test="${empty p.moderationLabel}">
                                                    <span class="text-gray-300 text-xs">—</span>
                                                </c:if>
                                            </td>
                                            <td class="py-3 pr-4 text-sm text-gray-500">${p.createdAt}</td>
                                            <td class="py-3">
                                                <div class="flex items-center gap-1">
                                                    <c:if test="${p.status == 'PENDING'}">
                                                        <button onclick="approvePost(${p.id})"
                                                            class="text-xs text-green-500 hover:text-green-700 transition px-2 py-1 rounded-lg hover:bg-green-50 font-medium">
                                                            <i class="fas fa-check"></i> Duyệt
                                                        </button>
                                                        <button onclick="rejectPost(${p.id})"
                                                            class="text-xs text-red-400 hover:text-red-600 transition px-2 py-1 rounded-lg hover:bg-red-50 font-medium">
                                                            <i class="fas fa-times"></i> Từ chối
                                                        </button>
                                                    </c:if>
                                                    <c:if test="${p.status == 'REJECTED'}">
                                                        <button onclick="approvePost(${p.id})"
                                                            class="text-xs text-green-500 hover:text-green-700 transition px-2 py-1 rounded-lg hover:bg-green-50 font-medium">
                                                            <i class="fas fa-check"></i> Duyệt lại
                                                        </button>
                                                    </c:if>
                                                    <button onclick="deletePost(${p.id})"
                                                        class="text-xs text-gray-400 hover:text-red-500 transition px-2 py-1 rounded-lg hover:bg-red-50">
                                                        <i class="fas fa-trash"></i> Xóa
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- ═══════════════════════════════════════════════════ -->
                    <!--  ANNOUNCEMENTS MANAGEMENT                          -->
                    <!-- ═══════════════════════════════════════════════════ -->
                    <div class="bg-white rounded-2xl shadow-sm p-5 mt-6">
                        <div class="flex items-center justify-between mb-4">
                            <h2 class="font-semibold text-gray-900"><i
                                    class="fas fa-bullhorn text-primary mr-2"></i>Quản lý Thông báo</h2>
                            <button onclick="document.getElementById('addAnnouncementModal').classList.remove('hidden')"
                                class="text-xs bg-primary hover:bg-primary-dark text-white font-semibold px-4 py-2 rounded-full transition">
                                <i class="fas fa-plus mr-1"></i>Tạo thông báo
                            </button>
                        </div>
                        <div class="space-y-3" id="announcementsList">
                            <c:forEach var="ann" items="${announcements}">
                                <div class="flex items-start gap-3 p-3 bg-gray-50 rounded-xl" id="ann-row-${ann.id}">
                                    <div
                                        class="w-8 h-8 bg-yellow-100 rounded-lg flex items-center justify-center flex-shrink-0">
                                        <i class="fas fa-bullhorn text-yellow-600 text-sm"></i>
                                    </div>
                                    <div class="flex-1 min-w-0">
                                        <p class="font-semibold text-sm text-gray-900">${ann.title}</p>
                                        <p class="text-xs text-gray-500 mt-0.5 truncate">${ann.content}</p>
                                        <p class="text-xs text-gray-400 mt-1">${ann.createdAt} — ${ann.admin.fullName}
                                        </p>
                                    </div>
                                    <button onclick="deleteAnnouncement(${ann.id})"
                                        class="text-xs text-gray-400 hover:text-red-500 transition px-2 py-1 rounded-lg hover:bg-red-50 flex-shrink-0">
                                        <i class="fas fa-trash"></i>
                                    </button>
                                </div>
                            </c:forEach>
                            <c:if test="${empty announcements}">
                                <p class="text-sm text-gray-400 text-center py-4">Chưa có thông báo nào.</p>
                            </c:if>
                        </div>
                    </div>
                </div>
            </main>

            <!-- Add User Modal -->
            <div id="addUserModal"
                class="hidden fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                <div class="bg-white rounded-2xl shadow-xl p-6 w-full max-w-sm">
                    <h3 class="font-bold text-gray-900 text-lg mb-4">Thêm người dùng mới</h3>
                    <div class="space-y-3">
                        <input type="text" id="newUsername" placeholder="Username"
                            class="w-full px-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-primary">
                        <input type="text" id="newFullName" placeholder="Họ và tên"
                            class="w-full px-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-primary">
                        <input type="password" id="newPassword" placeholder="Mật khẩu"
                            class="w-full px-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-primary">
                    </div>
                    <div class="flex gap-3 mt-5">
                        <button onclick="createUser()"
                            class="flex-1 bg-primary hover:bg-primary-dark text-white font-semibold py-2.5 rounded-xl text-sm transition">Tạo</button>
                        <button onclick="document.getElementById('addUserModal').classList.add('hidden')"
                            class="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold py-2.5 rounded-xl text-sm transition">Hủy</button>
                    </div>
                </div>
            </div>

            <!-- Add Announcement Modal -->
            <div id="addAnnouncementModal"
                class="hidden fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                <div class="bg-white rounded-2xl shadow-xl p-6 w-full max-w-sm">
                    <h3 class="font-bold text-gray-900 text-lg mb-4"><i
                            class="fas fa-bullhorn text-primary mr-2"></i>Tạo thông báo</h3>
                    <div class="space-y-3">
                        <input type="text" id="annTitle" placeholder="Tiêu đề thông báo"
                            class="w-full px-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-primary">
                        <textarea id="annContent" rows="3" placeholder="Nội dung chi tiết..."
                            class="w-full px-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-primary resize-none"></textarea>
                    </div>
                    <div class="flex gap-3 mt-5">
                        <button onclick="createAnnouncement()"
                            class="flex-1 bg-primary hover:bg-primary-dark text-white font-semibold py-2.5 rounded-xl text-sm transition">Đăng</button>
                        <button onclick="document.getElementById('addAnnouncementModal').classList.add('hidden')"
                            class="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold py-2.5 rounded-xl text-sm transition">Hủy</button>
                    </div>
                </div>
            </div>
            <div id="toast" class="fixed top-20 right-5 z-50 hidden">
                <div class="bg-white rounded-xl shadow-lg border px-5 py-3 flex items-center gap-3 min-w-[300px]">
                    <i id="toastIcon" class="fas fa-check-circle text-green-500 text-lg"></i>
                    <span id="toastMessage" class="text-sm text-gray-700 font-medium"></span>
                </div>
            </div>

            <script>
                var CTX = document.body.dataset.ctx || '';
                var CSRF_TOKEN = document.body.dataset.csrftoken || '';
                var currentTab = 'all';

                /**
                 * Helper: POST request kèm CSRF token.
                 */
                function adminPost(params) {
                    return fetch(CTX + '/AdminServlet', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded',
                            'X-CSRF-TOKEN': CSRF_TOKEN
                        },
                        body: params.toString()
                    });
                }

                // ═══════════════════════════════════════════════
                //  TOAST NOTIFICATION
                // ═══════════════════════════════════════════════
                function showToast(message, type) {
                    var toast = document.getElementById('toast');
                    var icon = document.getElementById('toastIcon');
                    var msg = document.getElementById('toastMessage');
                    msg.textContent = message;
                    if (type === 'success') {
                        icon.className = 'fas fa-check-circle text-green-500 text-lg';
                        toast.firstElementChild.style.borderColor = '#dcfce7';
                    } else if (type === 'error') {
                        icon.className = 'fas fa-exclamation-circle text-red-500 text-lg';
                        toast.firstElementChild.style.borderColor = '#fee2e2';
                    } else {
                        icon.className = 'fas fa-info-circle text-blue-500 text-lg';
                        toast.firstElementChild.style.borderColor = '#dbeafe';
                    }
                    toast.classList.remove('hidden');
                    toast.style.animation = 'fadeIn 0.3s ease';
                    setTimeout(function () { toast.classList.add('hidden'); }, 3000);
                }

                // ═══════════════════════════════════════════════
                //  MODERATION MODE
                // ═══════════════════════════════════════════════
                function setModerationMode(mode) {
                    var params = new URLSearchParams();
                    params.append('action', 'setModerationMode');
                    params.append('mode', mode);
                    adminPost(params)
                        .then(function (r) {
                            if (r.status === 403) { showToast('Lỗi CSRF token. Hãy tải lại trang.', 'error'); throw new Error('CSRF'); }
                            return r.json();
                        })
                        .then(function (data) {
                            if (data.error) { showToast(data.error, 'error'); return; }
                            if (data.status === 'ok') {
                                // Update UI
                                document.querySelectorAll('.mode-card').forEach(function (card) {
                                    card.classList.remove('active');
                                    card.querySelector('.mode-icon').classList.remove('bg-primary', 'text-white');
                                    card.querySelector('.mode-icon').classList.add('bg-gray-200', 'text-gray-500');
                                });
                                var activeCard = document.getElementById('mode-' + mode);
                                activeCard.classList.add('active');
                                activeCard.querySelector('.mode-icon').classList.remove('bg-gray-200', 'text-gray-500');
                                activeCard.querySelector('.mode-icon').classList.add('bg-primary', 'text-white');

                                var modeNames = { 'NONE': 'Không kiểm duyệt', 'MANUAL': 'Kiểm duyệt thủ công', 'AUTO_AI': 'Tự động (AI)' };
                                showToast('Đã chuyển sang: ' + modeNames[mode], 'success');
                            }
                        }).catch(function (e) { if (e.message !== 'CSRF') { console.error(e); showToast('Lỗi đổi chế độ', 'error'); } });
                }

                // ═══════════════════════════════════════════════
                //  CHECK AI SERVICE (dùng GET, không cần CSRF)
                // ═══════════════════════════════════════════════
                function checkAiService() {
                    fetch(CTX + '/AdminServlet?type=checkAiService')
                        .then(function (r) { return r.json(); })
                        .then(function (data) {
                            var dot = document.querySelector('.ai-status-dot');
                            var text = dot.nextElementSibling;
                            if (data.available) {
                                dot.className = 'ai-status-dot ai-online';
                                text.className = 'text-green-600 font-medium';
                                text.textContent = 'AI Service: Đang chạy';
                                showToast('AI Service đang hoạt động!', 'success');
                            } else {
                                dot.className = 'ai-status-dot ai-offline';
                                text.className = 'text-red-500 font-medium';
                                text.textContent = 'AI Service: Không khả dụng';
                                showToast('AI Service không khả dụng! (' + (data.url || '') + ')', 'error');
                            }
                        }).catch(function (e) { showToast('Lỗi kiểm tra AI Service', 'error'); });
                }

                // ═══════════════════════════════════════════════
                //  TABS
                // ═══════════════════════════════════════════════
                function switchTab(tab, btn) {
                    currentTab = tab;
                    // Update tab buttons
                    document.querySelectorAll('.tab-btn').forEach(function (b) { b.classList.remove('active'); });
                    btn.classList.add('active');

                    // Show/hide bulk actions
                    var bulkActions = document.getElementById('bulkActions');
                    bulkActions.style.display = tab === 'pending' ? 'flex' : 'none';

                    // Filter rows
                    document.querySelectorAll('.post-row').forEach(function (row) {
                        if (tab === 'all') {
                            row.style.display = '';
                        } else {
                            row.style.display = row.dataset.status === tab.toUpperCase() ? '' : 'none';
                        }
                    });
                }

                // ═══════════════════════════════════════════════
                //  POST MODERATION ACTIONS
                // ═══════════════════════════════════════════════
                function approvePost(postId) {
                    var params = new URLSearchParams();
                    params.append('action', 'approvePost');
                    params.append('postId', postId);
                    adminPost(params)
                        .then(function (r) { return r.json(); })
                        .then(function (data) {
                            if (data.status === 'ok') {
                                showToast('Đã duyệt bài #' + postId, 'success');
                                setTimeout(function () { location.reload(); }, 800);
                            }
                        }).catch(function (e) { showToast('Lỗi duyệt bài', 'error'); });
                }

                function rejectPost(postId) {
                    if (!confirm('Từ chối bài viết #' + postId + '?')) return;
                    var params = new URLSearchParams();
                    params.append('action', 'rejectPost');
                    params.append('postId', postId);
                    adminPost(params)
                        .then(function (r) { return r.json(); })
                        .then(function (data) {
                            if (data.status === 'ok') {
                                showToast('Đã từ chối bài #' + postId, 'success');
                                setTimeout(function () { location.reload(); }, 800);
                            }
                        }).catch(function (e) { showToast('Lỗi từ chối bài', 'error'); });
                }

                function approveAllPending() {
                    if (!confirm('Duyệt tất cả bài đang chờ?')) return;
                    var params = new URLSearchParams();
                    params.append('action', 'approveAll');
                    adminPost(params)
                        .then(function (r) { return r.json(); })
                        .then(function (data) {
                            if (data.status === 'ok') {
                                showToast('Đã duyệt ' + data.count + ' bài viết!', 'success');
                                setTimeout(function () { location.reload(); }, 800);
                            }
                        }).catch(function (e) { showToast('Lỗi duyệt tất cả', 'error'); });
                }

                // ═══════════════════════════════════════════════
                //  USER & POST MANAGEMENT (existing)
                // ═══════════════════════════════════════════════
                function deleteUser(userId) {
                    if (!confirm('Xóa người dùng này? Thao tác này sẽ xóa mọi dữ liệu liên quan!')) return;
                    var params = new URLSearchParams();
                    params.append('action', 'deleteUser');
                    params.append('userId', userId);
                    adminPost(params)
                        .then(function (r) { return r.json(); })
                        .then(function (data) {
                            if (data.error) {
                                alert(data.error);
                                return;
                            }
                            document.getElementById('user-row-' + userId).remove();
                            var stat = document.getElementById('statUsers');
                            stat.textContent = parseInt(stat.textContent) - 1;
                            showToast('Đã xóa người dùng', 'success');
                        }).catch(function (e) { console.error(e); alert('Lỗi xóa'); });
                }

                function deletePost(postId) {
                    if (!confirm('Xóa bài viết này?')) return;
                    var params = new URLSearchParams();
                    params.append('action', 'deletePost');
                    params.append('postId', postId);
                    adminPost(params)
                        .then(function (r) { return r.json(); })
                        .then(function (data) {
                            if (data.error) {
                                alert(data.error);
                                return;
                            }
                            document.getElementById('post-row-' + postId).remove();
                            showToast('Đã xóa bài viết', 'success');
                        }).catch(function (e) { console.error(e); alert('Lỗi xóa'); });
                }

                function createUser() {
                    var username = document.getElementById('newUsername').value.trim();
                    var fullName = document.getElementById('newFullName').value.trim();
                    var password = document.getElementById('newPassword').value.trim();
                    if (!username || !fullName || !password) { alert('Vui lòng điền đầy đủ'); return; }
                    var params = new URLSearchParams();
                    params.append('action', 'createUser');
                    params.append('username', username);
                    params.append('fullName', fullName);
                    params.append('password', password);
                    adminPost(params)
                        .then(async function (r) {
                            if (!r.ok) {
                                var data = await r.json();
                                throw new Error(data.error || 'Server error');
                            }
                            return r.json();
                        })
                        .then(function (data) {
                            document.getElementById('addUserModal').classList.add('hidden');
                            showToast('Tạo người dùng thành công!', 'success');
                            setTimeout(function () { location.reload(); }, 800);
                        }).catch(function (err) { alert(err.message); });
                }

                // ═══════════════════════════════════════════════
                //  ANNOUNCEMENTS
                // ═══════════════════════════════════════════════
                function createAnnouncement() {
                    var title = document.getElementById('annTitle').value.trim();
                    var content = document.getElementById('annContent').value.trim();
                    if (!title) { alert('Tiêu đề không được để trống'); return; }
                    var params = new URLSearchParams();
                    params.append('action', 'createAnnouncement');
                    params.append('title', title);
                    params.append('content', content);
                    adminPost(params)
                        .then(function (r) { return r.json(); })
                        .then(function (data) {
                            if (data.status === 'ok') {
                                document.getElementById('addAnnouncementModal').classList.add('hidden');
                                showToast('Đã tạo thông báo!', 'success');
                                setTimeout(function () { location.reload(); }, 800);
                            }
                        }).catch(function (e) { showToast('Lỗi tạo thông báo', 'error'); });
                }

                function deleteAnnouncement(annId) {
                    if (!confirm('Xóa thông báo này?')) return;
                    var params = new URLSearchParams();
                    params.append('action', 'deleteAnnouncement');
                    params.append('announcementId', annId);
                    adminPost(params)
                        .then(function (r) { return r.json(); })
                        .then(function (data) {
                            if (data.status === 'ok') {
                                var row = document.getElementById('ann-row-' + annId);
                                if (row) row.remove();
                                showToast('Đã xóa thông báo', 'success');
                            }
                        }).catch(function (e) { showToast('Lỗi xóa thông báo', 'error'); });
                }
            </script>
        </body>

        </html>
