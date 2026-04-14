// ==========================================
// TẦNG XỬ LÝ GIAO DIỆN CHAT (UI CONTROLLER)
// ==========================================

// Biến trạng thái toàn cục (State Management) cho giao diện Chat
let currentChatUser = null; // Lưu username của người đang chat cùng hiện tại
let lastLoadedMessageId = 0; // Đóng vai trò là "Cursor" (Con trỏ) để lấy tin nhắn mới nhất, tránh tải lại tin nhắn cũ

/**
 * Hàm bảo mật cực kỳ quan trọng: Ngăn chặn tấn công XSS (Cross-Site Scripting).
 * Bất cứ khi nào in nội dung user nhập ra màn hình (HTML), phải chuyển đổi các ký tự đặc biệt
 * để trình duyệt không hiểu lầm đó là mã lệnh script độc hại.
 */
function escapeHtml(unsafe) {
    if (!unsafe) return "";
    return String(unsafe)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// KHỞI TẠO LẮNG NGHE SỰ KIỆN KHI TRANG ĐÃ TẢI XONG
document.addEventListener('DOMContentLoaded', () => {
    setupMessageForm(); // Bật form nhập tin nhắn & xử lý ảnh
    setupFriendSearch(); // Bật tính năng tìm kiếm bạn bè ở sidebar

    // 1. TỰ ĐỘNG CHỌN NGƯỜI CHAT (Xử lý param từ URL)
    // Nếu chuyển từ trang danh sách bạn bè sang (có biến CHAT_WITH), tự động mở cửa sổ chat với người đó.
    if (typeof CHAT_WITH !== 'undefined' && CHAT_WITH && CHAT_WITH.trim() !== '') {
        const item = document.querySelector(`.chat-contact-item[data-username="${CHAT_WITH}"]`);
        const fullName = item ? item.dataset.fullname : CHAT_WITH;
        const avatarUrl = item ? item.dataset.avatar : '';
        selectChat(CHAT_WITH, fullName, avatarUrl);
    }

    // 2. LẮNG NGHE SỰ KIỆN ĐỒNG BỘ THÔNG BÁO (Từ api-client.js bắn sang)
    // Cập nhật các chấm đỏ (Badge) trên danh sách bạn bè ở thanh Sidebar.
    window.addEventListener('messageStatsUpdated', (e) => {
        const stats = e.detail;
        if (!stats || !stats.unreadCounts) return;

        // Quét toàn bộ danh sách bạn bè, so khớp username và vẽ lại chấm đỏ
        document.querySelectorAll('.chat-contact-item').forEach(item => {
            const username = item.dataset.username;
            const badge = item.querySelector('.unread-badge');
            if (!badge) return;

            const count = stats.unreadCounts[username];
            if (count && count > 0) {
                badge.textContent = count > 99 ? '99+' : count;
                badge.classList.remove('hidden');
                item.querySelector('.font-semibold').classList.add('text-red-600'); // Đổi màu chữ cho nổi bật
            } else {
                badge.classList.add('hidden');
                item.querySelector('.font-semibold').classList.remove('text-red-600');
            }
        });
    });

    // 3. LẮNG NGHE SỰ KIỆN WEBSOCKET (REAL-TIME REACTION)
    // Đây là nơi UI phản ứng ngay lập tức với các tín hiệu đẩy từ Server xuống.
    window.addEventListener('wsMessage', (e) => {
        const payload = e.detail;
        if (!payload) return;

        if (payload.type === 'NEW_MESSAGE') {
            // Chỉ tải lại tin nhắn NẾU tin nhắn mới đó thuộc về người mình đang mở cửa sổ chat.
            // Nếu là người khác gửi, đoạn code cập nhật Badge (ở trên) sẽ lo việc hiện chấm đỏ.
            if (payload.senderUsername === currentChatUser) {
                loadChatHistory(true); // true = Bắt buộc cuộn xuống đáy để xem tin mới
            }
        } else if (payload.type === 'MESSAGE_RECALLED') {
            // Có người thu hồi tin nhắn
            if (payload.partnerUsername === currentChatUser) {
                // Reset lại toàn bộ khung chat để render lại trạng thái "Đã thu hồi" cho tin nhắn đó
                document.getElementById('chatMessages').innerHTML = '';
                lastLoadedMessageId = 0;
                loadChatHistory(false); // false = Không cần ép cuộn màn hình
            }
        } else if (payload.type === 'MESSAGES_READ') {
            // Đối phương đã xem tin nhắn của mình
            if (payload.readerUsername === currentChatUser) {
                // Real-time update UI: Sửa text "Đã gửi" thành "Đã xem" mà không cần gọi API tải lại lịch sử.
                document.querySelectorAll('.msg-status:not(.is-read)').forEach(el => {
                    el.classList.add('is-read');
                    el.innerHTML = '<i class="fas fa-check-double text-blue-300"></i> Đã xem';

                    const bubble = el.closest('.msg-bubble');
                    if (bubble && bubble.hasAttribute('title')) {
                        const time = bubble.dataset.time || "";
                        bubble.setAttribute('title', `[Đã xem] - ${time}`);
                    }
                });
            }
        } else if (payload.type === 'UNFRIENDED' || payload.type === 'UNFRIENDED_SELF') {
            // Bị hủy kết bạn hoặc tự hủy kết bạn -> Đóng băng khung chat ngay lập tức
            if (payload.partnerUsername === currentChatUser) {
                document.getElementById('chatInputArea').classList.add('hidden');
                const p = document.getElementById('notFriendPlaceholder');
                if (p) p.classList.remove('hidden');
            }
        } else if (payload.type === 'FRIEND_REQUEST_ACCEPTED') {
            // Vừa thành bạn bè -> Mở khóa khung chat
            if (payload.partnerUsername === currentChatUser) {
                document.getElementById('chatInputArea').classList.remove('hidden');
                const p = document.getElementById('notFriendPlaceholder');
                if (p) p.classList.add('hidden');
            }
        }
    });
});

/**
 * Xử lý tìm kiếm bạn bè trên Sidebar (Client-side filtering)
 * Việc lọc diễn ra hoàn toàn trên trình duyệt (ẩn/hiện DOM), giúp phản hồi tức thì khi gõ phím.
 */
function setupFriendSearch() {
    const searchInput = document.getElementById('friendSearchInput');
    if (!searchInput) return;

    searchInput.addEventListener('input', function (e) {
        const query = e.target.value.toLowerCase().trim();
        const items = document.getElementById('friendsList').querySelectorAll('.chat-contact-item');

        items.forEach(item => {
            const name = item.dataset.fullname ? item.dataset.fullname.toLowerCase() : '';
            const username = item.dataset.username ? item.dataset.username.toLowerCase() : '';

            // Tìm theo cả tên đầy đủ hoặc username
            if (name.includes(query) || username.includes(query)) {
                item.style.display = 'flex';
            } else {
                item.style.display = 'none';
            }
        });
    });
}

// Format lại chuỗi thời gian chuẩn ISO sang định dạng dễ đọc (DD/MM HH:mm)
function formatMessageTime(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr.replace('T', ' ')); // Cắt bỏ chữ T trong ISO format nếu có
    const hours = d.getHours().toString().padStart(2, '0');
    const minutes = d.getMinutes().toString().padStart(2, '0');
    const day = d.getDate().toString().padStart(2, '0');
    const month = (d.getMonth() + 1).toString().padStart(2, '0');

    return `${day}/${month} ${hours}:${minutes}`;
}

/**
 * Hàm kích hoạt khi Click vào một người bạn trên danh sách
 * Nhiệm vụ: Thay đổi Header, dọn dẹp khung chat cũ và gọi API lấy lịch sử chat mới.
 */
function selectChat(username, fullName, avatarUrl) {
    currentChatUser = username; // Cập nhật State

    // --- Cập nhật Giao diện Header ---
    document.getElementById('chatTitle').textContent = fullName || username;
    const partnerAvImg = document.getElementById('chatHeaderAvatarImg');
    const partnerAvNoImg = document.getElementById('chatHeaderAvatarNoImg');

    // Xử lý logic hiển thị Avatar (Có ảnh gốc vs Avatar chữ cái mặc định)
    if (avatarUrl && avatarUrl.trim() !== '') {
        partnerAvImg.src = avatarUrl;
        partnerAvImg.classList.remove('hidden');
        partnerAvNoImg.classList.add('hidden');
    } else {
        partnerAvImg.classList.add('hidden');
        partnerAvNoImg.classList.remove('hidden');
        partnerAvNoImg.textContent = (fullName || username).charAt(0).toUpperCase();
    }

    const profileLink = document.getElementById('chatHeaderProfileLink');
    if (profileLink) {
        profileLink.href = `${CTX}/ProfileServlet?username=${encodeURIComponent(username)}`;
    }

    // Chuyển đổi các khối (Block) hiển thị
    document.getElementById('chatWindowHeader').classList.remove('hidden');
    document.getElementById('chatPlaceholder').classList.add('hidden'); // Ẩn hình nền "Chọn người để chat"
    document.getElementById('chatMessages').classList.remove('hidden');
    document.getElementById('chatInputArea').classList.add('hidden'); // Tạm ẩn form nhập, chờ API check xem còn là bạn bè không

    // Highlight item đang chọn trên Sidebar
    document.querySelectorAll('.chat-contact-item').forEach(item => {
        item.classList.toggle('active', item.dataset.username === username);
    });

    // Reset lịch sử chat nội bộ và kéo dữ liệu mới
    document.getElementById('chatMessages').innerHTML = '';
    const emptyPlaceholder = document.getElementById('emptyChatPlaceholder');
    if (emptyPlaceholder) emptyPlaceholder.classList.add('hidden');

    lastLoadedMessageId = 0; // Trả cursor về 0
    loadChatHistory(true);
}

/**
 * Lõi API: Tải lịch sử tin nhắn
 * @param {boolean} scrollBottom - Có cuộn màn hình xuống dưới cùng sau khi tải xong không
 */
async function loadChatHistory(scrollBottom) {
    if (!currentChatUser) return;
    try {
        // Gửi Cursor (lastLoadedMessageId) lên server để server chỉ trả về những tin nhắn "mới hơn" id này
        const url = `${CTX}/ChatServlet?action=history&otherUser=${encodeURIComponent(currentChatUser)}&lastMessageId=${lastLoadedMessageId}`;
        const responseData = await apiFetch(url);

        const messages = responseData.messages || [];
        const isFriend = responseData.isFriend;

        const msgsContainer = document.getElementById('chatMessages');
        const emptyPlaceholder = document.getElementById('emptyChatPlaceholder');
        const chatInputArea = document.getElementById('chatInputArea');
        const notFriendPlaceholder = document.getElementById('notFriendPlaceholder');

        // Cập nhật trạng thái "Còn là bạn bè không?" để khóa/mở form nhập tin nhắn
        if (isFriend) {
            chatInputArea.classList.remove('hidden');
            if (notFriendPlaceholder) notFriendPlaceholder.classList.add('hidden');
        } else {
            chatInputArea.classList.add('hidden');
            if (notFriendPlaceholder) notFriendPlaceholder.classList.remove('hidden');
        }

        // Kịch bản: Không có tin nhắn nào mới được trả về
        if (!messages || messages.length === 0) {
            // Vẫn phải check xem có ai xem tin nhắn cũ của mình không
            if (responseData.lastReadMessageId) {
                document.querySelectorAll('.msg-status').forEach(el => {
                    const msgId = parseInt(el.dataset.id);
                    if (msgId <= responseData.lastReadMessageId && !el.classList.contains('is-read')) {
                        el.classList.add('is-read');
                        el.innerHTML = '<i class="fas fa-check-double text-blue-300"></i> Đã xem';
                    }
                });
            }
            // Nếu cursor vẫn là 0 (nghĩa là cuộc hội thoại trắng trơn), hiện placeholder "Chưa có tin nhắn"
            if (lastLoadedMessageId === 0) {
                msgsContainer.classList.add('hidden');
                emptyPlaceholder.classList.remove('hidden');
            }
            return;
        }

        // Có tin nhắn mới
        emptyPlaceholder.classList.add('hidden');
        msgsContainer.classList.remove('hidden');

        // Render từng bong bóng chat
        messages.forEach((msg, idx) => {
            const type = msg.senderUsername === CURRENT_USER.username ? 'sent' : 'received';
            // Cờ đánh dấu tin nhắn cuối cùng (Chỉ hiện trạng thái Đã gửi/Đã xem ở bong bóng cuối cùng cho đỡ rối mắt)
            const isLastNode = (idx === messages.length - 1);

            appendMessage(msg, type, isLastNode, msgsContainer);

            // Cập nhật Cursor bằng ID lớn nhất
            if (msg.id > lastLoadedMessageId) {
                lastLoadedMessageId = msg.id;
            }
        });

        // Quét lại trạng thái Đã Xem
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

        // Quan trọng: Sau khi load xong và hiển thị ra màn hình, báo cho Server biết "TÔI ĐÃ ĐỌC NHỮNG TIN NÀY RỒI"
        apiFetch(CTX + '/ChatServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ action: 'markRead', senderUsername: currentChatUser }).toString()
        }, false).then(() => {
            // Cập nhật lại các chấm đỏ trên thanh điều hướng toàn cục
            if (typeof fetchGlobalMessageStats === 'function') {
                fetchGlobalMessageStats();
            }
        });

    } catch (e) {
        console.error("Lỗi tải chat:", e);
    }
}

/**
 * Hàm Render HTML sinh ra bong bóng chat (Message Bubble)
 */
function appendMessage(message, type, isLastNode, container) {
    const msgs = container || document.getElementById('chatMessages');
    const timeStr = formatMessageTime(message.timestamp);
    const div = document.createElement('div');

    // type === 'sent': Căn phải (tin của mình). type === 'received': Căn trái (tin đối tác)
    div.className = `flex ${type === 'sent' ? 'justify-end' : 'justify-start'} mb-1 relative group/msg`;

    let contentHtml = "";
    if (message.isRecalled) {
        contentHtml = `<div class="italic text-gray-400 text-xs py-1">Tin nhắn đã bị thu hồi</div>`;
    } else {
        // Xử lý hiển thị ảnh (nếu có) và chữ.
        // Có sự kiện onload ở ảnh để ép cửa sổ chat tự cuộn xuống đáy khi ảnh load xong (tránh bị lỗi chiều cao).
        const img = message.imageUrl ? `<img src="${message.imageUrl}" class="w-full rounded-lg mb-2 cursor-pointer max-h-64 object-cover" onclick="window.open('${message.imageUrl}', '_blank')" onload="const c=document.getElementById('chatMessages'); if(c) c.scrollTop=c.scrollHeight;">` : '';
        const txt = message.content ? `<div>${escapeHtml(message.content)}</div>` : '';
        contentHtml = img + txt;
    }

    // Nút "Thu hồi" (Chỉ hiện khi hover vào tin nhắn do chính mình gửi và chưa bị thu hồi)
    const recallBtn = (type === 'sent' && !message.isRecalled)
        ? `<button onclick="recallMessage(${message.id})" class="absolute top-1/2 -left-8 -translate-y-1/2 opacity-0 group-hover/msg:opacity-100 text-gray-400 hover:text-red-500 transition p-1" title="Thu hồi tin nhắn">
             <i class="fas fa-undo-alt text-xs"></i>
           </button>`
        : '';

    const statusText = message.isRead ? 'Đã xem' : 'Đã gửi';
    const tooltipText = (type === 'sent') ? `[${statusText}] - ${timeStr}` : timeStr;

    // Lắp ráp HTML phức tạp (Bong bóng, Nút thu hồi, Trạng thái, Thời gian)
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

// Gọi API thu hồi tin nhắn
async function recallMessage(messageId) {
    if (!confirm("Bạn có chắc chắn muốn thu hồi tin nhắn này? Cả hai phía đều sẽ không thấy nội dung này nữa.")) return;
    try {
        await apiFetch(CTX + '/ChatServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ action: 'recall', messageId: messageId }).toString()
        });
        // Tái tạo lại UI sau khi thu hồi
        document.getElementById('chatMessages').innerHTML = '';
        lastLoadedMessageId = 0;
        loadChatHistory(false);
    } catch (e) {
        console.error("Recall error:", e);
    }
}

// Xóa lịch sử (Chỉ tác dụng phía Client hiện tại, phía đối phương không mất)
async function clearHistory() {
    if (!currentChatUser) return;
    if (!confirm(`Bạn có chắc muốn xóa toàn bộ lịch sử trò chuyện với ${currentChatUser}?`)) return;

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

/**
 * Xử lý khu vực gửi tin nhắn (Bao gồm form Text và Upload Ảnh)
 */
function setupMessageForm() {
    const form = document.getElementById('messageForm');
    const attachBtn = document.getElementById('chatAttachBtn');
    const imageInput = document.getElementById('chatImageInput');
    const previewContainer = document.getElementById('imagePreviewContainer');
    const previewImg = document.getElementById('chatImagePreview');
    const cancelPreviewBtn = document.getElementById('cancelImagePreview');

    if (!form) return;

    // Nút đính kèm ảnh (Chuyển tiếp sự kiện click vào thẻ <input type="file" hidden>)
    if (attachBtn) {
        attachBtn.addEventListener('click', () => imageInput.click());
    }

    // Xử lý tạo hình ảnh xem trước (Preview) bằng FileReader (Đọc trực tiếp file trên RAM trình duyệt, chưa gửi lên Server)
    if (imageInput) {
        imageInput.addEventListener('change', function () {
            if (this.files && this.files[0]) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    previewImg.src = e.target.result; // Base64 url
                    previewContainer.classList.remove('hidden');
                }
                reader.readAsDataURL(this.files[0]);
            }
        });
    }

    // Nút xóa ảnh đã chọn
    if (cancelPreviewBtn) {
        cancelPreviewBtn.addEventListener('click', () => {
            imageInput.value = '';
            previewContainer.classList.add('hidden');
            previewImg.src = '';
        });
    }

    // Xử lý luồng GỬI TIN NHẮN
    form.addEventListener('submit', async (e) => {
        e.preventDefault(); // Ngăn trình duyệt tự tải lại trang
        if (!currentChatUser) return;

        const content = document.getElementById('messageInput').value.trim();
        const hasImage = imageInput && imageInput.files && imageInput.files.length > 0;

        if (!content && !hasImage) return; // Nếu form trống thì bỏ qua

        try {
            let selectedImageUrl = "";

            // LUỒNG 1: Nếu có ảnh, phải upload ảnh lên Servlet riêng để lấy URL về trước
            if (hasImage) {
                const formData = new FormData();
                formData.append('imageFile', imageInput.files[0]);
                const uploadResult = await apiFetch(CTX + '/UploadChatImage', {
                    method: 'POST',
                    body: formData // Gửi dạng Multipart/form-data
                });
                if (uploadResult.imageUrl) {
                    selectedImageUrl = uploadResult.imageUrl;
                } else {
                    alert('Lỗi tải lên ảnh');
                    return;
                }
            }

            // LUỒNG 2: Bắn nội dung Text + URL Ảnh (nếu có) vào luồng Chat
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

            // Dọn dẹp form sau khi gửi thành công
            document.getElementById('messageInput').value = '';
            if (cancelPreviewBtn) cancelPreviewBtn.click();

            // Tải lại lịch sử chat để cập nhật bong bóng mới của mình (Kéo cursor lên)
            await loadChatHistory(true);
        } catch (e) {
            console.error("Lỗi gửi tin nhắn:", e);
        }
    });
}
