const _bodyData = document.body.dataset || {};
var CTX = _bodyData.ctx || '';
var CURRENT_USER = {
    username: _bodyData.username || '',
    fullName: _bodyData.fullname || '',
    avatar: _bodyData.avatar || '',
    role: _bodyData.role || ''
};
var CSRF_TOKEN = _bodyData.csrftoken || '';

/**
 * @param {string} url - Đường dẫn URL (nên bao gồm CTX ở trước)
 * @param {object} options - Các tùy chọn fetch (method, headers, body...)
 * @param {boolean} returnJson - Mặc định true. Trả về promise parse JSON nếu true, ngược lại trả về response gốc.
 * @returns {Promise<any>}
 */
async function apiFetch(url, options = {}, returnJson = true) {
    try {
        if (!options.headers) {
            options.headers = {};
        }
        if (options.method && ['POST', 'PUT', 'DELETE'].includes(options.method.toUpperCase())) {
            options.headers['X-CSRF-TOKEN'] = CSRF_TOKEN;
        }
        const response = await fetch(url, options);

        if (!response.ok) {
            if (response.status === 401) {
                alert("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
                window.location.href = CTX + '/AuthServlet?action=login';
                throw new Error("Unauthorized");
            } else if (response.status === 400) {
                try {
                    const data = await response.json();
                    if (data.error) alert(data.error);
                } catch (e) { }
                throw new Error("Bad Request: " + response.statusText);
            } else if (response.status >= 500) {
                alert("Lỗi máy chủ " + response.status + ". Vui lòng thử lại sau!");
                throw new Error("Server Error");
            } else {
                console.error("Lỗi HTTP:", response.status, response.statusText);
            }
        }

        if (returnJson) {
            try {
                return await response.json();
            } catch (err) {
                return {};
            }
        }

        return response;
    } catch (error) {
        console.error("API Fetch Error:", error);
        throw error;
    }
}

// Global Message Websocket
document.addEventListener('DOMContentLoaded', () => {
    const globalBadge = document.getElementById('globalMessageBadge');
    if (!CURRENT_USER || !CURRENT_USER.username) return;

    let chatSocket = null;
    let reconnectTimer = null;

    async function fetchGlobalMessageStats() {
        try {
            const data = await apiFetch(CTX + '/ChatServlet?action=stats');
            if (data && typeof data.totalUnread !== 'undefined') {
                if (globalBadge) {
                    if (data.totalUnread > 0) {
                        globalBadge.textContent = data.totalUnread > 99 ? '99+' : data.totalUnread;
                        globalBadge.classList.remove('hidden');
                    } else {
                        globalBadge.classList.add('hidden');
                    }
                }
                const friendBadge = document.getElementById('globalFriendBadge');
                if (friendBadge && typeof data.friendRequestsCount !== 'undefined') {
                    if (data.friendRequestsCount > 0) {
                        friendBadge.textContent = data.friendRequestsCount > 99 ? '99+' : data.friendRequestsCount;
                        friendBadge.classList.remove('hidden');
                    } else {
                        friendBadge.classList.add('hidden');
                    }
                }
                // Ném sự kiện messageStatsUpadted ra cho các file JS khác sử dụng (VD: chat-servlet.js)
                window.dispatchEvent(new CustomEvent('messageStatsUpdated', { detail: data }));
            }
        } catch (e) {
            // Mất 1 vài sự kiện ko ảnh hưởng quá lớn nên bỏ qua
        }
    }

    // cho hàm fetchGlobalMessageStats ở toàn cục (global)
    window.fetchGlobalMessageStats = fetchGlobalMessageStats;

    function initWebSocket() {
        if (chatSocket && (chatSocket.readyState === WebSocket.OPEN || chatSocket.readyState === WebSocket.CONNECTING)) {
            return;
        }

        const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = wsProtocol + '//' + window.location.host + CTX + '/ws/chat';

        chatSocket = new WebSocket(wsUrl);

        chatSocket.onopen = function () {
            console.log('Chat WebSocket Connected');
            clearTimeout(reconnectTimer);
        };

        chatSocket.onmessage = function (event) {
            try {
                const payload = JSON.parse(event.data);

                // Triggers an event for UI components to listen
                window.dispatchEvent(new CustomEvent('wsMessage', { detail: payload }));

                // Update global UI consistency by fetching correct unread maps
                const updateTriggers = ['NEW_MESSAGE', 'MESSAGES_READ', 'FRIEND_REQUEST', 'FRIEND_REQUEST_ACCEPTED', 'FRIEND_REQUEST_REJECTED', 'UNFRIENDED', 'UNFRIENDED_SELF', 'FRIEND_REQUEST_CANCELLED'];
                if (updateTriggers.includes(payload.type)) {
                    fetchGlobalMessageStats();
                }

            } catch (e) {
                console.error("Lỗi phân tích WebSocket payload", e);
            }
        };

        chatSocket.onclose = function () {
            // Auto reconnect on drop
            reconnectTimer = setTimeout(initWebSocket, 3000);
        };

        chatSocket.onerror = function () {
            chatSocket.close();
        };
    }

    // Initial fetch
    fetchGlobalMessageStats();

    // Connect WebSocket
    initWebSocket();
});
