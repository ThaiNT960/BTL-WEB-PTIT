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
            item.classList.add('bg-red-50');
        } else {
            item.classList.remove('bg-red-50');
        }
    });

    // Clear messages and load history
    document.getElementById('chatMessages').innerHTML = '';
    lastMessageCount = 0;
    loadChatHistory(true);

    // Start polling
    if (pollingInterval) clearInterval(pollingInterval);
    pollingInterval = setInterval(() => loadChatHistory(false), 3000);
}

async function loadChatHistory(scrollBottom) {
    if (!currentChatUser) return;
    try {
        const res = await fetch(`${CTX}/ChatServlet?action=history&otherUser=${encodeURIComponent(currentChatUser)}`);
        const messages = await res.json();

        // Only update if new messages arrived
        if (messages.length > lastMessageCount) {
            const msgs = document.getElementById('chatMessages');
            msgs.innerHTML = '';
            messages.forEach(msg => {
                const type = msg.senderUsername === CURRENT_USER.username ? 'sent' : 'received';
                appendMessage(msg, type, msgs);
            });
            lastMessageCount = messages.length;
            if (scrollBottom) msgs.scrollTop = msgs.scrollHeight;
        }
    } catch (e) { console.error(e); }
}

function appendMessage(message, type, container) {
    const msgs = container || document.getElementById('chatMessages');
    const timeStr = formatMessageTime(message.timestamp);
    const div = document.createElement('div');
    div.className = `flex ${type === 'sent' ? 'justify-end' : 'justify-start'} mb-1`;
    div.innerHTML = `
        <div class="max-w-xs lg:max-w-md px-4 py-2.5 rounded-2xl text-sm leading-relaxed
            ${type === 'sent'
            ? 'bg-primary text-white rounded-br-sm'
            : 'bg-white text-gray-800 shadow-sm rounded-bl-sm'}">
            <div>${message.content}</div>
            <div class="text-xs mt-1 ${type === 'sent' ? 'text-red-200' : 'text-gray-400'}">${timeStr}</div>
        </div>`;
    msgs.appendChild(div);
    msgs.scrollTop = msgs.scrollHeight;
}

function setupMessageForm() {
    const form = document.getElementById('messageForm');
    if (!form) return;
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!currentChatUser) return;
        const content = document.getElementById('messageInput').value.trim();
        if (!content) return;
        try {
            const formData = new FormData();
            formData.append('action', 'send');
            formData.append('receiverUsername', currentChatUser);
            formData.append('content', content);
            await fetch(CTX + '/ChatServlet', { method: 'POST', body: formData });
            document.getElementById('messageInput').value = '';
            // Immediately show and refresh
            await loadChatHistory(true);
        } catch (e) { console.error(e); }
    });
}
