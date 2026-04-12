// chat-servlet.js - dùng AJAX polling thay WebSocket
// Gọi /ChatServlet?action=history mỗi 3 giây để nhận tin nhắn mới

let currentChatUser = null;
let pollingInterval = null;
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
    // If chatWith param provided (from friend.jsp), auto-select
    if (CHAT_WITH && CHAT_WITH.trim() !== '') {
        const item = document.querySelector(`.chat-contact-item[data-username="${CHAT_WITH}"]`);
        const fullName = item ? item.dataset.fullname : CHAT_WITH;
        const avatarUrl = item ? item.dataset.avatar : '';
        selectChat(CHAT_WITH, fullName, avatarUrl);
    }
});

function setupFriendSearch() {
    const searchInput = document.getElementById('friendSearchInput');
    if (!searchInput) return;

    searchInput.addEventListener('input', function(e) {
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
    return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
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

    // Start polling
    if (pollingInterval) clearInterval(pollingInterval);
    pollingInterval = setInterval(() => loadChatHistory(false), 3000);
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
            if (lastLoadedMessageId === 0) {
                msgsContainer.classList.add('hidden');
                emptyPlaceholder.classList.remove('hidden');
            }
            return;
        }

        emptyPlaceholder.classList.add('hidden');
        msgsContainer.classList.remove('hidden');

        // Render all returned messages chronologically
        messages.forEach(msg => {
            const type = msg.senderUsername === CURRENT_USER.username ? 'sent' : 'received';
            appendMessage(msg, type, msgsContainer);
            if (msg.id > lastLoadedMessageId) {
                lastLoadedMessageId = msg.id;
            }
        });
        
        if (scrollBottom) msgsContainer.scrollTop = msgsContainer.scrollHeight;
    } catch (e) { 
        console.error("Lỗi tải chat:", e);
        // If error persists, maybe show a small toast or warning
    }
}

function appendMessage(message, type, container) {
    const msgs = container || document.getElementById('chatMessages');
    const timeStr = formatMessageTime(message.timestamp);
    const div = document.createElement('div');
    div.className = `flex ${type === 'sent' ? 'justify-end' : 'justify-start'} mb-1`;
    const imgHtml = message.imageUrl ? `<img src="${message.imageUrl}" class="w-full rounded-lg mb-2 cursor-pointer max-h-64 object-cover" onclick="window.open('${message.imageUrl}', '_blank')" onload="const c=document.getElementById('chatMessages'); if(c) c.scrollTop=c.scrollHeight;">` : '';
    const contentHtml = message.content ? `<div>${escapeHtml(message.content)}</div>` : '';

    div.innerHTML = `
        <div class="max-w-xs lg:max-w-md px-4 py-2.5 rounded-2xl text-sm leading-relaxed
            ${type === 'sent'
            ? 'bg-primary text-white rounded-br-sm'
            : 'bg-white text-gray-800 shadow-sm rounded-bl-sm'}">
            ${imgHtml}
            ${contentHtml}
            <div class="text-xs mt-1 ${type === 'sent' ? 'text-red-200' : 'text-gray-400'}">${timeStr}</div>
        </div>`;
    msgs.appendChild(div);
    msgs.scrollTop = msgs.scrollHeight;
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
        imageInput.addEventListener('change', function() {
            if (this.files && this.files[0]) {
                const reader = new FileReader();
                reader.onload = function(e) {
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

