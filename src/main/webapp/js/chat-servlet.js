// chat-servlet.js - dùng AJAX polling thay WebSocket
// Gọi /ChatServlet?action=history mỗi 3 giây để nhận tin nhắn mới

let currentChatUser = null;
let pollingInterval = null;
let lastMessageCount = 0;

document.addEventListener('DOMContentLoaded', () => {
    setupMessageForm();
    // If chatWith param provided (from friend.jsp), auto-select
    if (CHAT_WITH && CHAT_WITH.trim() !== '') {
        const item = document.querySelector(`.chat-contact-item[data-username="${CHAT_WITH}"]`);
        const fullName = item ? item.dataset.fullname : CHAT_WITH;
        selectChat(CHAT_WITH, fullName);
    }
});

function formatMessageTime(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr.replace('T', ' '));
    return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
}

function selectChat(username, fullName) {
    currentChatUser = username;

    // Update header
    document.getElementById('chatTitle').textContent = fullName || username;
    const partnerAv = document.getElementById('chatPartnerAvatar');
    if (partnerAv) partnerAv.textContent = (fullName || username).charAt(0).toUpperCase();
    document.getElementById('chatWindowHeader').classList.remove('hidden');
    document.getElementById('chatPlaceholder').classList.add('hidden');
    document.getElementById('chatMessages').classList.remove('hidden');
    document.getElementById('chatInputArea').classList.remove('hidden');

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
    
    lastMessageCount = 0;
    loadChatHistory(true);

    // Start polling
    if (pollingInterval) clearInterval(pollingInterval);
    pollingInterval = setInterval(() => loadChatHistory(false), 3000);
}

async function loadChatHistory(scrollBottom) {
    if (!currentChatUser) return;
    try {
        const url = `${CTX}/ChatServlet?action=history&otherUser=${encodeURIComponent(currentChatUser)}`;
        const messages = await apiFetch(url);

        const msgsContainer = document.getElementById('chatMessages');
        const emptyPlaceholder = document.getElementById('emptyChatPlaceholder');

        if (!messages || messages.length === 0) {
            msgsContainer.classList.add('hidden');
            emptyPlaceholder.classList.remove('hidden');
            lastMessageCount = 0;
            return;
        }

        emptyPlaceholder.classList.add('hidden');
        msgsContainer.classList.remove('hidden');

        // Only update if new messages arrived
        if (messages.length > lastMessageCount) {
            // Lấy ra các tin nhắn mới từ mảng trả về
            const newMessages = messages.slice(lastMessageCount);
            
            newMessages.forEach(msg => {
                const type = msg.senderUsername === CURRENT_USER.username ? 'sent' : 'received';
                appendMessage(msg, type, msgsContainer);
            });
            
            lastMessageCount = messages.length;
            if (scrollBottom) msgsContainer.scrollTop = msgsContainer.scrollHeight;
        }
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
    const imgHtml = message.imageUrl ? `<img src="${message.imageUrl}" class="w-full rounded-lg mb-2 cursor-pointer max-h-64 object-cover" onclick="window.open('${message.imageUrl}', '_blank')">` : '';
    const contentHtml = message.content ? `<div>${message.content}</div>` : '';

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
                const uploadRes = await fetch(CTX + '/UploadChatImage', {
                    method: 'POST',
                    body: formData
                });
                const uploadResult = await uploadRes.json();
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
            alert('Có lỗi xảy ra khi gửi tin nhắn');
        }
    });
}

