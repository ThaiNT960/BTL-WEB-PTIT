// Event delegation for accept / reject buttons (avoids inline onclick + JSP expr mixing)
document.addEventListener('click', function (e) {
    const btn = e.target.closest('.js-friend-action');
    if (!btn) return;
    const reqId = btn.dataset.reqId;
    const username = btn.dataset.username;
    const action = btn.dataset.action;
    
    if (action === 'accept') acceptRequest(reqId, btn);
    if (action === 'accept_by_username') acceptRequestByUsername(username, btn);
    if (action === 'reject') rejectRequest(reqId, btn);
    if (action === 'unfriend') unfriend(username, btn);
    if (action === 'cancel') cancelRequest(username, btn);
});

function escapeHtml(unsafe) {
    if (!unsafe) return "";
    return String(unsafe)
         .replace(/&/g, "&amp;")
         .replace(/</g, "&lt;")
         .replace(/>/g, "&gt;")
         .replace(/"/g, "&quot;")
         .replace(/'/g, "&#039;");
}

async function searchFriend() {
    const query = document.getElementById('searchInput').value.trim();
    if (!query) return;
    const container = document.getElementById('searchResults');
    container.innerHTML = `<p class="text-center text-gray-400 text-sm py-4">Đang tìm kiếm...</p>`;
    try {
        const users = await apiFetch(CTX + '/SearchServlet?keyword=' + encodeURIComponent(query));
        if (!users || !users.length) {
            container.innerHTML = `<p class="text-center text-gray-400 text-sm py-4">Không tìm thấy người dùng nào</p>`;
            return;
        }
        container.innerHTML = users.map(u => {
            const eFullName = escapeHtml(u.fullName || u.username);
            const eUsername = escapeHtml(u.username);
            const eAvatar = escapeHtml(u.avatar);
            
            const initial = eFullName.charAt(0).toUpperCase();
            const avt = eAvatar ? `<img src="${eAvatar}" class="w-11 h-11 rounded-full object-cover flex-shrink-0" onerror="this.outerHTML='<div class=\\'w-11 h-11 rounded-full bg-primary flex items-center justify-center text-white font-bold flex-shrink-0\\'>${initial}</div>'">` : `<div class="w-11 h-11 rounded-full bg-primary flex items-center justify-center text-white font-bold flex-shrink-0">${initial}</div>`;
            
            let actionHtml = '';
            const status = u.relationshipStatus;
            
            if (status === 'FRIENDS') {
                actionHtml = `
                    <div class="flex gap-2">
                        <a href="${CTX}/chat?chatWith=${eUsername}" class="text-xs bg-gray-100 hover:bg-gray-200 text-gray-600 font-medium px-3 py-1.5 rounded-full transition">
                             <i class="fas fa-comment-dots"></i>
                        </a>
                        <button data-username="${eUsername}" data-action="unfriend" class="js-friend-action text-xs bg-red-50 hover:bg-red-100 text-red-600 font-medium px-3 py-1.5 rounded-full transition">
                            Hủy
                        </button>
                    </div>`;
            } else if (status === 'PENDING_SENT') {
                actionHtml = `
                    <button data-username="${eUsername}" data-action="cancel" class="js-friend-action text-xs bg-gray-100 hover:bg-gray-200 text-gray-500 font-medium px-3 py-1.5 rounded-full transition">
                        Thu hồi
                    </button>`;
            } else if (status === 'PENDING_RECEIVED') {
                 actionHtml = `
                    <div class="flex gap-2">
                        <button data-username="${eUsername}" data-action="accept_by_username" class="js-friend-action text-xs bg-primary hover:bg-primary-dark text-white font-semibold px-3 py-1.5 rounded-full transition">
                            Ok
                        </button>
                    </div>`;
            } else {
                actionHtml = `
                    <button onclick="sendRequest('${eUsername}', this)" class="text-xs bg-primary hover:bg-primary-dark text-white font-semibold px-3 py-1.5 rounded-full transition">
                        <i class="fas fa-user-plus"></i> Kết bạn
                    </button>`;
            }

            return `
            <div class="flex items-center gap-3 py-3 border-b border-gray-100 last:border-0" id="user-row-${eUsername}">
                ${avt}
                <div class="flex-1 min-w-0">
                    <p class="font-semibold text-sm text-gray-900">${eFullName}</p>
                    <p class="text-xs text-gray-400">@${eUsername}</p>
                </div>
                <div class="action-wrap">
                    ${actionHtml}
                </div>
            </div>`;
        }).join('');
    } catch (e) {
        container.innerHTML = `<p class="text-center text-red-400 text-sm py-4">Lỗi tìm kiếm</p>`;
    }
}

async function sendRequest(receiverUsername, btn) {
    try {
        const params = new URLSearchParams();
        params.append('action', 'request');
        params.append('receiverUsername', receiverUsername);

        const res = await apiFetch(CTX + '/FriendServlet', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded' // Ép cứng header
            },
            body: params.toString() // Chuyển thành chuỗi query để Servlet chắc chắn đọc được
        }, false);

        if (res && res.ok) {
            btn.textContent = 'Đã gửi';
            btn.disabled = true;
            btn.className = 'text-xs bg-gray-200 text-gray-500 font-medium px-3 py-1.5 rounded-full';
        } else if (res && !res.ok) {
            console.error('Lỗi server:', res.status);
        }
    } catch (e) { console.error(e); }
}

async function acceptRequest(requestId, btn) {
    try {
        const params = new URLSearchParams();
        params.append('action', 'accept');
        params.append('requestId', requestId);

        const res = await apiFetch(CTX + '/FriendServlet', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: params.toString()
        }, false);

        if (res && res.ok) {
            const row = btn.closest('.flex.items-center');
            if (row) {
                const btnGroup = row.querySelector('.flex.gap-2');
                if (btnGroup) btnGroup.innerHTML = `<span class="text-xs text-green-600 font-medium"><i class="fas fa-check"></i> Đã chấp nhận</span>`;
            }
        }
    } catch (e) { console.error(e); }
}

async function acceptRequestByUsername(username, btn) {
    try {
        const params = new URLSearchParams();
        params.append('action', 'accept_by_username');
        params.append('senderUsername', username);

        const res = await apiFetch(CTX + '/FriendServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        }, false);

        if (res && res.ok) {
            btn.innerHTML = '<i class="fas fa-check"></i> Xong';
            btn.disabled = true;
            btn.className = 'text-xs text-green-600 font-medium px-3 py-1.5';
        }
    } catch (e) { console.error(e); }
}

async function rejectRequest(requestId, btn) {
    try {
        const params = new URLSearchParams();
        params.append('action', 'reject');
        params.append('requestId', requestId);

        const res = await apiFetch(CTX + '/FriendServlet', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: params.toString()
        }, false);

        if (res && res.ok) {
            const row = btn.closest('.flex.items-center.gap-3');
            if (row) row.remove();
        }
    } catch (e) { console.error(e); }
}

async function unfriend(username, btn) {
    if (!confirm('Bạn có chắc chắn muốn hủy kết bạn?')) return;
    try {
        const params = new URLSearchParams();
        params.append('action', 'unfriend');
        params.append('targetUsername', username);

        const res = await apiFetch(CTX + '/FriendServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        }, false);

        if (res && res.ok) {
            if (typeof refreshFriendData === 'function') refreshFriendData();
        }
    } catch (e) { console.error(e); }
}

async function cancelRequest(username, btn) {
    try {
        const params = new URLSearchParams();
        params.append('action', 'cancel');
        params.append('targetUsername', username);

        const res = await apiFetch(CTX + '/FriendServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        }, false);

        if (res && res.ok) {
            btn.textContent = 'Đã thu hồi';
            btn.disabled = true;
            btn.className = 'text-xs bg-gray-50 text-gray-400 font-medium px-3 py-1.5 rounded-full';
        }
    } catch (e) { console.error(e); }
}

async function fetchAndRenderRequests() {
    const container = document.getElementById('friendRequests');
    if (!container) return; // Only process if on friend.jsp
    try {
        const requests = await apiFetch(CTX + '/FriendServlet?type=requests');
        if (!requests || requests.length === 0) {
            container.innerHTML = '<p class="text-center text-gray-400 text-sm py-6">Không có lời mời kết bạn nào</p>';
            return;
        }
        
        container.innerHTML = requests.map(req => {
            const eFullName = escapeHtml(req.senderFullName || req.senderUsername);
            const eUsername = escapeHtml(req.senderUsername);
            const initial = eFullName.charAt(0).toUpperCase();
            const eAvatar = escapeHtml(req.senderAvatar || '');
            const avtHtml = eAvatar ? `<img src="${eAvatar}" class="w-11 h-11 rounded-full object-cover">` : `<div class="w-11 h-11 rounded-full bg-primary flex items-center justify-center text-white font-bold">${initial}</div>`;
            
            return `
            <div class="flex items-center gap-3 py-3 border-b border-gray-100 last:border-0">
                <a href="${CTX}/ProfileServlet?username=${eUsername}" class="flex-shrink-0 hover:opacity-80 transition">
                    ${avtHtml}
                </a>
                <div class="flex-1 min-w-0">
                    <a href="${CTX}/ProfileServlet?username=${eUsername}" class="hover:underline">
                        <p class="font-semibold text-sm text-gray-900">${eFullName}</p>
                    </a>
                </div>
                <div class="flex gap-2">
                    <button data-req-id="${req.id}" data-action="accept" class="js-friend-action text-xs bg-primary hover:bg-primary-dark text-white font-semibold px-3 py-1.5 rounded-full transition">Chấp nhận</button>
                    <button data-req-id="${req.id}" data-action="reject" class="js-friend-action text-xs bg-gray-100 hover:bg-gray-200 text-gray-600 font-medium px-3 py-1.5 rounded-full transition">Từ chối</button>
                </div>
            </div>`;
        }).join('');
    } catch (e) { console.error(e); }
}

async function fetchAndRenderFriends() {
    const container = document.getElementById('friendsList');
    if (!container) return; // Only process if on friend.jsp
    try {
        const friends = await apiFetch(CTX + '/FriendServlet?type=friends');
        // Update counter in Header
        const headerCount = document.querySelector('h2 > i.fa-user-friends')?.parentElement;
        if (headerCount) {
             headerCount.innerHTML = `<i class="fas fa-user-friends text-primary mr-2"></i>Bạn bè (${friends ? friends.length : 0})`;
        }

        if (!friends || friends.length === 0) {
            container.innerHTML = '<p class="text-center text-gray-400 text-sm py-6">Bạn chưa có bạn bè nào</p>';
            return;
        }

        container.innerHTML = friends.map(f => {
            const eFullName = escapeHtml(f.fullName || f.username);
            const eUsername = escapeHtml(f.username);
            const eAvatar = escapeHtml(f.avatar);
            const initial = eFullName.charAt(0).toUpperCase();
            const avtHtml = eAvatar ? `<img src="${eAvatar}" class="w-11 h-11 rounded-full object-cover">` : `<div class="w-11 h-11 rounded-full bg-primary flex items-center justify-center text-white font-bold">${initial}</div>`;
            
            return `
            <div class="flex items-center gap-3 py-3 border-b border-gray-100 last:border-0">
                <a href="${CTX}/ProfileServlet?username=${eUsername}" class="flex-shrink-0 hover:opacity-80 transition">
                    ${avtHtml}
                </a>
                <div class="flex-1 min-w-0">
                    <a href="${CTX}/ProfileServlet?username=${eUsername}" class="hover:underline">
                        <p class="font-semibold text-sm text-gray-900">${eFullName}</p>
                    </a>
                </div>
                <div class="flex gap-2">
                    <a href="${CTX}/ChatServlet?chatWith=${eUsername}" class="text-xs bg-gray-100 hover:bg-gray-200 text-gray-600 font-medium px-3 py-1.5 rounded-full transition">
                        <i class="fas fa-comment-dots"></i> Nhắn tin
                    </a>
                    <button data-username="${eUsername}" data-action="unfriend" class="js-friend-action text-xs bg-red-50 hover:bg-red-100 text-red-600 font-medium px-3 py-1.5 rounded-full transition">
                        Hủy kết bạn
                    </button>
                </div>
            </div>`;
        }).join('');
    } catch (e) { console.error(e); }
}

function refreshFriendData() {
    fetchAndRenderRequests();
    fetchAndRenderFriends();
}

window.addEventListener('wsMessage', (e) => {
    const payload = e.detail;
    if (!payload) return;
    const updateTriggers = ['FRIEND_REQUEST', 'FRIEND_REQUEST_ACCEPTED', 'FRIEND_REQUEST_REJECTED', 'UNFRIENDED', 'UNFRIENDED_SELF', 'FRIEND_REQUEST_CANCELLED'];
    if (updateTriggers.includes(payload.type)) {
        refreshFriendData();
    }
});
