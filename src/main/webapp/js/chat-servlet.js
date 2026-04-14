let currentChatUser = null;
let lastLoadedMessageId = 0;

function escapeHtml(unsafe) {
    if (!unsafe) return "";
    return String(unsafe)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

document.addEventListener('DOMContentLoaded', () => {
    setupMessageForm();
    setupFriendSearch();
    // Cập nhật giao diện chat nếu url chỉ đến 1 đoạn chat cụ thể
    if (CHAT_WITH && CHAT_WITH.trim() !== '') {
        const item = document.querySelector(`.chat-contact-item[data-username="${CHAT_WITH}"]`);
        const fullName = item ? item.dataset.fullname : CHAT_WITH;
        const avatarUrl = item ? item.dataset.avatar : '';
        selectChat(CHAT_WITH, fullName, avatarUrl);
    }

    //Lắng nghe sự kiện global messageStatsUpdated từ api-client.js để cập nhật unread badges trên sidebar
    window.addEventListener('messageStatsUpdated', (e) => {
        const stats = e.detail;
        if (!stats || !stats.unreadCounts) return;

        document.querySelectorAll('.chat-contact-item').forEach(item => {
            const username = item.dataset.username;
            const badge = item.querySelector('.unread-badge');
            if (!badge) return;

            const count = stats.unreadCounts[username];
            if (count && count > 0) {
                badge.textContent = count > 99 ? '99+' : count;
                badge.classList.remove('hidden');
                item.querySelector('.font-semibold').classList.add('text-red-600');
            } else {
                badge.classList.add('hidden');
                item.querySelector('.font-semibold').classList.remove('text-red-600');
            }
        });
    });

    // Listen to WebSocket events dynamically
    window.addEventListener('wsMessage', (e) => {
        const payload = e.detail;
        if (!payload) return;

        if (payload.type === 'NEW_MESSAGE') {
            if (payload.senderUsername === currentChatUser || payload.receiverUsername === currentChatUser) {
                // If the message is from/to our currently active chat window, load it immediately
                loadChatHistory(true);
            }
        } else if (payload.type === 'MESSAGE_RECALLED') {
            if (payload.partnerUsername === currentChatUser) {
                // Refresh full chat to see the updated recall status of old messages
                document.getElementById('chatMessages').innerHTML = '';
                lastLoadedMessageId = 0;
                loadChatHistory(false);
            }
        } else if (payload.type === 'MESSAGES_READ') {
            if (payload.readerUsername === currentChatUser) {
                // Mark all our sent messages as read immediately
                document.querySelectorAll('.msg-status:not(.is-read)').forEach(el => {
                    el.classList.add('is-read');
                    el.innerHTML = '<i class="fas fa-check-double text-blue-300"></i> Đã xem';

                    // Also update the tooltip of the bubble
                    const bubble = el.closest('.msg-bubble');
                    if (bubble && bubble.hasAttribute('title')) {
                        const time = bubble.dataset.time || "";
                        bubble.setAttribute('title', `[Đã xem] - ${time}`);
                    }
                });
            }
        } else if (payload.type === 'UNFRIENDED' || payload.type === 'UNFRIENDED_SELF') {
            if (payload.partnerUsername === currentChatUser) {
                document.getElementById('chatInputArea').classList.add('hidden');
                const p = document.getElementById('notFriendPlaceholder');
                if (p) p.classList.remove('hidden');
            }
        } else if (payload.type === 'FRIEND_REQUEST_ACCEPTED') {
            if (payload.partnerUsername === currentChatUser) {
                document.getElementById('chatInputArea').classList.remove('hidden');
                const p = document.getElementById('notFriendPlaceholder');
                if (p) p.classList.add('hidden');
            }
        }
    });
});

function setupFriendSearch() {
    const searchInput = document.getElementById('friendSearchInput');
    if (!searchInput) return;

    searchInput.addEventListener('input', function (e) {
        const query = e.target.value.toLowerCase().trim();
        const friendsList = document.getElementById('friendsList');
        const items = friendsList.querySelectorAll('.chat-contact-item');

        items.forEach(item => {
            const name = item.dataset.fullname ? item.dataset.fullname.toLowerCase() : '';
            const username = item.dataset.username ? item.dataset.username.toLowerCase() : '';

            if (name.includes(query) || username.includes(query)) {
                item.style.display = 'flex';
            } else {
                item.style.display = 'none';
            }
        });
    });
}

function formatMessageTime(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr.replace('T', ' '));
    const hours = d.getHours().toString().padStart(2, '0');
    const minutes = d.getMinutes().toString().padStart(2, '0');
    const day = d.getDate().toString().padStart(2, '0');
    const month = (d.getMonth() + 1).toString().padStart(2, '0');

    // Now (current time) to compare year if needed, but per request 12/04 15:30 style
    return `${day}/${month} ${hours}:${minutes}`;
}

function selectChat(username, fullName, avatarUrl) {
    currentChatUser = username;

    // Update header
    document.getElementById('chatTitle').textContent = fullName || username;
    const partnerAvImg = document.getElementById('chatHeaderAvatarImg');
    const partnerAvNoImg = document.getElementById('chatHeaderAvatarNoImg');

    if (avatarUrl && avatarUrl.trim() !== '') {
        partnerAvImg.src = avatarUrl;
        partnerAvImg.style.display = '';
        partnerAvImg.classList.remove('hidden');
        partnerAvNoImg.style.display = '';
        partnerAvNoImg.classList.add('hidden');
    } else {
        partnerAvImg.style.display = '';
        partnerAvImg.classList.add('hidden');
        partnerAvNoImg.style.display = '';
        partnerAvNoImg.classList.remove('hidden');
        partnerAvNoImg.textContent = (fullName || username).charAt(0).toUpperCase();
    }

    // Update profile link in header
    const profileLink = document.getElementById('chatHeaderProfileLink');
    if (profileLink) {
        profileLink.href = `${CTX}/ProfileServlet?username=${encodeURIComponent(username)}`;
    }

    document.getElementById('chatWindowHeader').classList.remove('hidden');
    document.getElementById('chatPlaceholder').classList.add('hidden');
    document.getElementById('chatMessages').classList.remove('hidden');

    // Hide input areas, wait for history API to toggle
    document.getElementById('chatInputArea').classList.add('hidden');
    const notFriendPlaceholder = document.getElementById('notFriendPlaceholder');
    if (notFriendPlaceholder) notFriendPlaceholder.classList.add('hidden');

    // Highlight active contact
    document.querySelectorAll('.chat-contact-item').forEach(item => {
        if (item.dataset.username === username) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });

    // Clear messages and load history
    document.getElementById('chatMessages').innerHTML = '';
    const emptyPlaceholder = document.getElementById('emptyChatPlaceholder');
    if (emptyPlaceholder) emptyPlaceholder.classList.add('hidden');

    lastLoadedMessageId = 0;
    loadChatHistory(true);
}

async function loadChatHistory(scrollBottom) {
    if (!currentChatUser) return;
    try {
        const url = `${CTX}/ChatServlet?action=history&otherUser=${encodeURIComponent(currentChatUser)}&lastMessageId=${lastLoadedMessageId}`;
        const responseData = await apiFetch(url);

        const messages = responseData.messages || [];
        const isFriend = responseData.isFriend;

        const msgsContainer = document.getElementById('chatMessages');
        const emptyPlaceholder = document.getElementById('emptyChatPlaceholder');
        const chatInputArea = document.getElementById('chatInputArea');
        const notFriendPlaceholder = document.getElementById('notFriendPlaceholder');

        // Show/hide input area based on isFriend
        if (isFriend) {
            chatInputArea.classList.remove('hidden');
            if (notFriendPlaceholder) notFriendPlaceholder.classList.add('hidden');
        } else {
            chatInputArea.classList.add('hidden');
            if (notFriendPlaceholder) notFriendPlaceholder.classList.remove('hidden');
        }

        if (!messages || messages.length === 0) {
            if (responseData.lastReadMessageId) {
                document.querySelectorAll('.msg-status').forEach(el => {
                    const msgId = parseInt(el.dataset.id);
                    if (msgId <= responseData.lastReadMessageId && !el.classList.contains('is-read')) {
                        el.classList.add('is-read');
                        el.innerHTML = '<i class="fas fa-check-double text-blue-300"></i> Đã xem';
                    }
                });
            }
            if (lastLoadedMessageId === 0) {
                msgsContainer.classList.add('hidden');
                emptyPlaceholder.classList.remove('hidden');
            }
            return;
        }

        emptyPlaceholder.classList.add('hidden');
        msgsContainer.classList.remove('hidden');

        // Render all returned messages chronologically
        messages.forEach((msg, idx) => {
            const type = msg.senderUsername === CURRENT_USER.username ? 'sent' : 'received';
            // isLastNode is ONLY true for the absolute last message in the sequence
            const isLastNode = (idx === messages.length - 1);

            appendMessage(msg, type, isLastNode, msgsContainer);
            if (msg.id > lastLoadedMessageId) {
                lastLoadedMessageId = msg.id;
            }
        });

        if (responseData.lastReadMessageId) {
            document.querySelectorAll('.msg-status').forEach(el => {
                const msgId = parseInt(el.dataset.id);
                if (msgId <= responseData.lastReadMessageId && !el.classList.contains('is-read')) {
                    el.classList.add('is-read');
                    el.innerHTML = '<i class="fas fa-check-double text-blue-300"></i> Đã xem';
                }
            });
        }

        if (scrollBottom) msgsContainer.scrollTop = msgsContainer.scrollHeight;

        // Mark as read explicitly if we fetched messages
        apiFetch(CTX + '/ChatServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ action: 'markRead', senderUsername: currentChatUser }).toString()
        }, false).then(() => {
            if (typeof fetchGlobalMessageStats === 'function') {
                fetchGlobalMessageStats();
            }
        });

    } catch (e) {
        console.error("Lỗi tải chat:", e);
        // If error persists, maybe show a small toast or warning
    }
}

function appendMessage(message, type, isLastNode, container) {
    const msgs = container || document.getElementById('chatMessages');
    const timeStr = formatMessageTime(message.timestamp);
    const div = document.createElement('div');
    div.className = `flex ${type === 'sent' ? 'justify-end' : 'justify-start'} mb-1 relative group/msg`;

    let contentHtml = "";
    if (message.isRecalled) {
        contentHtml = `<div class="italic text-gray-400 text-xs py-1">Tin nhắn đã bị thu hồi</div>`;
    } else {
        const img = message.imageUrl ? `<img src="${message.imageUrl}" class="w-full rounded-lg mb-2 cursor-pointer max-h-64 object-cover" onclick="window.open('${message.imageUrl}', '_blank')" onload="const c=document.getElementById('chatMessages'); if(c) c.scrollTop=c.scrollHeight;">` : '';
        const txt = message.content ? `<div>${escapeHtml(message.content)}</div>` : '';
        contentHtml = img + txt;
    }

    // Recall button for sent messages that are not already recalled
    const recallBtn = (type === 'sent' && !message.isRecalled)
        ? `<button onclick="recallMessage(${message.id})" class="absolute top-1/2 -left-8 -translate-y-1/2 opacity-0 group-hover/msg:opacity-100 text-gray-400 hover:text-red-500 transition p-1" title="Thu hồi tin nhắn">
             <i class="fas fa-undo-alt text-xs"></i>
           </button>`
        : '';

    // Advanced tooltip content
    const statusText = message.isRead ? 'Đã xem' : 'Đã gửi';
    const tooltipText = (type === 'sent') ? `[${statusText}] - ${timeStr}` : timeStr;

    div.innerHTML = `
        <div class="relative">
            ${recallBtn}
            <div class="msg-bubble max-w-xs lg:max-w-md px-4 py-2.5 rounded-2xl text-sm leading-relaxed cursor-default
                ${message.isRecalled ? 'bg-gray-100 border border-gray-200' : (type === 'sent' ? 'bg-primary text-white rounded-br-sm' : 'bg-white text-gray-800 shadow-sm rounded-bl-sm')}"
                ${!isLastNode ? `title="${tooltipText}"` : ''}
                data-time="${timeStr}">
                ${contentHtml}
                ${(isLastNode && !message.isRecalled) ? `
                <div class="text-[11px] mt-1.5 flex justify-between gap-3 items-center ${type === 'sent' ? 'text-red-100' : 'text-gray-400'}">
                    <span>${timeStr}</span>
                    ${type === 'sent' ? `<span class="msg-status ${message.isRead ? 'is-read' : ''} bg-black bg-opacity-20 px-2 py-0.5 rounded-full backdrop-blur-sm shadow-sm" data-id="${message.id}">${message.isRead ? '<i class="fas fa-check-double text-blue-300"></i> Đã xem' : '<i class="fas fa-check"></i> Đã gửi'}</span>` : ''}
                </div>` : ''}
            </div>
        </div>`;

    msgs.appendChild(div);
    msgs.scrollTop = msgs.scrollHeight;
}

async function recallMessage(messageId) {
    if (!confirm("Bạn có chắc chắn muốn thu hồi tin nhắn này? Cả hai phía đều sẽ không thấy nội dung này nữa.")) return;
    try {
        await apiFetch(CTX + '/ChatServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ action: 'recall', messageId: messageId }).toString()
        });
        // Clear and reload to update the UI specifically for the recalled message
        document.getElementById('chatMessages').innerHTML = '';
        lastLoadedMessageId = 0;
        loadChatHistory(false);
    } catch (e) {
        console.error("Recall error:", e);
    }
}

async function clearHistory() {
    if (!currentChatUser) return;
    if (!confirm(`Bạn có chắc muốn xóa toàn bộ lịch sử trò chuyện với ${currentChatUser}? Lưu ý: Chỉ xóa phía bạn, đối phương vẫn thấy.`)) return;

    try {
        await apiFetch(CTX + '/ChatServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ action: 'clearHistory', otherUser: currentChatUser }).toString()
        });
        document.getElementById('chatMessages').innerHTML = '';
        lastLoadedMessageId = 0;
        loadChatHistory(true);
    } catch (e) {
        console.error("Clear history error:", e);
    }
}

function setupMessageForm() {
    const form = document.getElementById('messageForm');
    const attachBtn = document.getElementById('chatAttachBtn');
    const imageInput = document.getElementById('chatImageInput');
    const previewContainer = document.getElementById('imagePreviewContainer');
    const previewImg = document.getElementById('chatImagePreview');
    const cancelPreviewBtn = document.getElementById('cancelImagePreview');

    if (!form) return;

    if (attachBtn) {
        attachBtn.addEventListener('click', () => imageInput.click());
    }

    if (imageInput) {
        imageInput.addEventListener('change', function () {
            if (this.files && this.files[0]) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    previewImg.src = e.target.result;
                    previewContainer.classList.remove('hidden');
                }
                reader.readAsDataURL(this.files[0]);
            }
        });
    }

    if (cancelPreviewBtn) {
        cancelPreviewBtn.addEventListener('click', () => {
            imageInput.value = '';
            previewContainer.classList.add('hidden');
            previewImg.src = '';
        });
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!currentChatUser) return;
        const content = document.getElementById('messageInput').value.trim();
        const hasImage = imageInput && imageInput.files && imageInput.files.length > 0;

        if (!content && !hasImage) return;

        try {
            let selectedImageUrl = "";

            if (hasImage) {
                const formData = new FormData();
                formData.append('imageFile', imageInput.files[0]);
                const uploadResult = await apiFetch(CTX + '/UploadChatImage', {
                    method: 'POST',
                    body: formData
                });
                if (uploadResult.imageUrl) {
                    selectedImageUrl = uploadResult.imageUrl;
                } else {
                    alert('Lỗi tải lên ảnh');
                    return;
                }
            }

            const params = new URLSearchParams();
            params.append('action', 'send');
            params.append('receiverUsername', currentChatUser);
            params.append('content', content);
            if (selectedImageUrl) {
                params.append('imageUrl', selectedImageUrl);
            }

            await apiFetch(CTX + '/ChatServlet', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params.toString()
            }, false);

            document.getElementById('messageInput').value = '';
            if (cancelPreviewBtn) cancelPreviewBtn.click();
            await loadChatHistory(true);
        } catch (e) {
            console.error("Lỗi gửi tin nhắn:", e);
        }
    });
}
