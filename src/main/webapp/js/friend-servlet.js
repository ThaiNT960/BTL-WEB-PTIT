// friend-servlet.js - dùng với friend.jsp, gọi /FriendServlet thay vì /api/friends

async function searchFriend() {
    const query = document.getElementById('searchInput').value.trim();
    if (!query) return;
    const container = document.getElementById('searchResults');
    container.innerHTML = `<p class="text-center text-gray-400 text-sm py-4">Đang tìm kiếm...</p>`;
    try {
        const res = await fetch(CTX + '/SearchServlet?keyword=' + encodeURIComponent(query));
        const users = await res.json();
        if (!users.length) {
            container.innerHTML = `<p class="text-center text-gray-400 text-sm py-4">Không tìm thấy người dùng nào</p>`;
            return;
        }
        container.innerHTML = users.map(u => {
            const initial = (u.fullName || u.username || '?').charAt(0).toUpperCase();
            return `
            <div class="flex items-center gap-3 py-3 border-b border-gray-100 last:border-0">
                <div class="w-11 h-11 rounded-full bg-primary flex items-center justify-center text-white font-bold flex-shrink-0">${initial}</div>
                <div class="flex-1 min-w-0">
                    <p class="font-semibold text-sm text-gray-900">${u.fullName || u.username}</p>
                    <p class="text-xs text-gray-400">@${u.username}</p>
                </div>
                <button onclick="sendRequest('${u.username}', this)" class="text-xs bg-primary hover:bg-primary-dark text-white font-semibold px-3 py-1.5 rounded-full transition">
                    <i class="fas fa-user-plus"></i> Kết bạn
                </button>
            </div>`;
        }).join('');
    } catch (e) {
        container.innerHTML = `<p class="text-center text-red-400 text-sm py-4">Lỗi tìm kiếm</p>`;
    }
}

async function sendRequest(receiverUsername, btn) {
    try {
        const form = new FormData();
        form.append('action', 'request');
        form.append('receiverUsername', receiverUsername);
        await fetch(CTX + '/FriendServlet', { method: 'POST', body: form });
        btn.textContent = 'Đã gửi';
        btn.disabled = true;
        btn.className = 'text-xs bg-gray-200 text-gray-500 font-medium px-3 py-1.5 rounded-full';
    } catch (e) { console.error(e); }
}

async function acceptRequest(requestId, btn) {
    try {
        const form = new FormData();
        form.append('action', 'accept');
        form.append('requestId', requestId);
        await fetch(CTX + '/FriendServlet', { method: 'POST', body: form });
        const row = btn.closest('.flex.items-center');
        if (row) {
            const btnGroup = row.querySelector('.flex.gap-2');
            if (btnGroup) btnGroup.innerHTML = `<span class="text-xs text-green-600 font-medium"><i class="fas fa-check"></i> Đã chấp nhận</span>`;
        }
    } catch (e) { console.error(e); }
}

async function rejectRequest(requestId, btn) {
    try {
        const form = new FormData();
        form.append('action', 'reject');
        form.append('requestId', requestId);
        await fetch(CTX + '/FriendServlet', { method: 'POST', body: form });
        const row = btn.closest('.flex.items-center.gap-3');
        if (row) row.remove();
    } catch (e) { console.error(e); }
}
