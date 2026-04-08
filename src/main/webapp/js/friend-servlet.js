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
            const initial = (u.fullName || u.username || '?').charAt(0).toUpperCase();
            const avt = u.avatar ? `<img src="${u.avatar}" class="w-11 h-11 rounded-full object-cover flex-shrink-0" onerror="this.outerHTML='<div class=\\'w-11 h-11 rounded-full bg-primary flex items-center justify-center text-white font-bold flex-shrink-0\\'>${initial}</div>'">` : `<div class="w-11 h-11 rounded-full bg-primary flex items-center justify-center text-white font-bold flex-shrink-0">${initial}</div>`;
            
            let actionHtml = '';
            const status = u.relationshipStatus;
            
            if (status === 'FRIENDS') {
                actionHtml = `
                    <div class="flex gap-2">
                        <a href="${CTX}/chat?chatWith=${u.username}" class="text-xs bg-gray-100 hover:bg-gray-200 text-gray-600 font-medium px-3 py-1.5 rounded-full transition">
                             <i class="fas fa-comment-dots"></i>
                        </a>
                        <button data-username="${u.username}" data-action="unfriend" class="js-friend-action text-xs bg-red-50 hover:bg-red-100 text-red-600 font-medium px-3 py-1.5 rounded-full transition">
                            Hủy
                        </button>
                    </div>`;
            } else if (status === 'PENDING_SENT') {
                actionHtml = `
                    <button data-username="${u.username}" data-action="cancel" class="js-friend-action text-xs bg-gray-100 hover:bg-gray-200 text-gray-500 font-medium px-3 py-1.5 rounded-full transition">
                        Thu hồi
                    </button>`;
            } else if (status === 'PENDING_RECEIVED') {
                 actionHtml = `
                    <div class="flex gap-2">
                        <button data-username="${u.username}" data-action="accept_by_username" class="js-friend-action text-xs bg-primary hover:bg-primary-dark text-white font-semibold px-3 py-1.5 rounded-full transition">
                            Ok
                        </button>
                    </div>`;
            } else {
                actionHtml = `
                    <button onclick="sendRequest('${u.username}', this)" class="text-xs bg-primary hover:bg-primary-dark text-white font-semibold px-3 py-1.5 rounded-full transition">
                        <i class="fas fa-user-plus"></i> Kết bạn
                    </button>`;
            }

            return `
            <div class="flex items-center gap-3 py-3 border-b border-gray-100 last:border-0" id="user-row-${u.username}">
                ${avt}
                <div class="flex-1 min-w-0">
                    <p class="font-semibold text-sm text-gray-900">${u.fullName || u.username}</p>
                    <p class="text-xs text-gray-400">@${u.username}</p>
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
            location.reload();
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
