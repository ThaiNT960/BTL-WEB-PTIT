// home-servlet.js - dùng với home.jsp, gọi /PostServlet thay vì /api/posts

document.addEventListener('DOMContentLoaded', () => {
    loadPosts();
    setupPostForm();
});

function formatTime(dateStr) {
    if (!dateStr) return '';
    const now = new Date();
    const d = new Date(dateStr.replace('T', ' '));
    const diffMs = now - d;
    const diffMin = Math.floor(diffMs / 60000);
    const diffHour = Math.floor(diffMin / 60);
    const diffDay = Math.floor(diffHour / 24);
    if (diffMin < 1) return 'Vừa xong';
    if (diffMin < 60) return `${diffMin} phút trước`;
    if (diffHour < 24) return `${diffHour} giờ trước`;
    if (diffDay < 30) return `${diffDay} ngày trước`;
    return d.toLocaleDateString('vi-VN');
}

async function loadPosts() {
    try {
        const res = await fetch(CTX + '/PostServlet');
        const posts = await res.json();
        const feed = document.getElementById('postsFeed');
        feed.innerHTML = '';
        if (!posts.length) {
            feed.innerHTML = `<div class="bg-white rounded-2xl shadow-sm p-8 text-center text-gray-400 text-sm">Chưa có bài viết nào. Hãy đăng bài đầu tiên!</div>`;
            return;
        }
        posts.forEach(post => renderPost(post, feed));
    } catch (e) { console.error(e); }
}

function renderPost(post, container) {
    const initials = (post.userFullName || post.username || '?').charAt(0).toUpperCase();
    const isOwner = post.username === CURRENT_USER.username;
    const isAdmin = CURRENT_USER.role === 'ROLE_ADMIN';
    const liked = post.liked;
    const likeCount = post.likeCount || 0;
    const commentCount = (post.comments || []).length;

    const div = document.createElement('div');
    div.id = `post-${post.id}`;
    div.className = 'bg-white rounded-2xl shadow-sm mb-4 overflow-hidden';
    div.innerHTML = `
        <div class="p-5">
            <div class="flex items-center gap-3 mb-3">
                <div class="w-10 h-10 rounded-full bg-primary flex items-center justify-center text-white font-bold text-sm flex-shrink-0">${initials}</div>
                <div class="flex-1 min-w-0">
                    <p class="font-semibold text-gray-900 text-sm">${post.userFullName || post.username}</p>
                    <p class="text-xs text-gray-400">${formatTime(post.createdAt)}</p>
                </div>
                ${(isOwner || isAdmin) ? `<button onclick="deletePost(${post.id})" class="text-gray-300 hover:text-red-500 transition text-sm px-2"><i class="fas fa-trash"></i></button>` : ''}
            </div>
            <p class="text-gray-800 text-sm leading-relaxed mb-3">${post.content}</p>
            ${post.imageUrl ? `<img src="${post.imageUrl}" alt="Post image" class="w-full rounded-xl mb-3 max-h-96 object-cover" onerror="this.style.display='none'">` : ''}
            <div class="flex items-center gap-4 text-xs text-gray-400 mb-3">
                <span id="like-count-${post.id}">${likeCount} lượt thích</span>
                <span>${commentCount} bình luận</span>
            </div>
        </div>
        <div class="border-t border-gray-100"></div>
        <div class="flex px-2 py-1">
            <button onclick="toggleLike(${post.id}, this)" class="flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl text-sm font-medium transition hover:bg-gray-50 ${liked ? 'text-primary' : 'text-gray-500'}" data-liked="${liked}">
                <i class="${liked ? 'fas' : 'far'} fa-heart"></i> Thích
            </button>
            <button onclick="toggleComments(${post.id})" class="flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl text-sm font-medium text-gray-500 hover:bg-gray-50 transition">
                <i class="far fa-comment"></i> Bình luận
            </button>
        </div>
        <div id="comments-${post.id}" class="hidden border-t border-gray-100 p-4 bg-gray-50">
            <div id="comments-list-${post.id}">
                ${(post.comments || []).map(c => `
                    <div class="flex gap-3 mb-3">
                        <div class="w-8 h-8 rounded-full bg-primary flex items-center justify-center text-white font-bold text-xs flex-shrink-0">
                            ${(c.userFullName || c.username || 'U').charAt(0).toUpperCase()}
                        </div>
                        <div class="bg-white rounded-xl px-3 py-2 flex-1 shadow-sm">
                            <p class="font-semibold text-xs text-gray-700 mb-0.5">${c.userFullName || c.username}</p>
                            <p class="text-sm text-gray-800">${c.content}</p>
                        </div>
                    </div>
                `).join('')}
            </div>
            <div class="flex gap-2 mt-2">
                <input type="text" id="comment-input-${post.id}" placeholder="Viết bình luận..."
                    class="flex-1 bg-white border border-gray-200 rounded-full px-4 py-2 text-sm focus:outline-none focus:border-primary transition"
                    onkeyup="if(event.key==='Enter') submitComment(${post.id})">
                <button onclick="submitComment(${post.id})" class="bg-primary hover:bg-primary-dark text-white text-xs font-semibold px-4 py-2 rounded-full transition">Gửi</button>
            </div>
        </div>
    `;
    container.appendChild(div);
}

function toggleComments(postId) {
    document.getElementById(`comments-${postId}`).classList.toggle('hidden');
}

async function toggleLike(postId, btn) {
    try {
        const form = new FormData();
        form.append('action', 'like');
        form.append('postId', postId);
        const res = await fetch(CTX + '/PostServlet', { method: 'POST', body: form });
        const data = await res.json();
        const liked = data.liked;
        btn.dataset.liked = liked;
        btn.className = `flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl text-sm font-medium transition hover:bg-gray-50 ${liked ? 'text-primary' : 'text-gray-500'}`;
        btn.innerHTML = `<i class="${liked ? 'fas' : 'far'} fa-heart"></i> Thích`;
        const countEl = document.getElementById(`like-count-${postId}`);
        if (countEl) countEl.textContent = `${data.likeCount} lượt thích`;
    } catch (e) { console.error(e); }
}

async function deletePost(postId) {
    if (!confirm('Xóa bài viết này?')) return;
    try {
        const form = new FormData();
        form.append('action', 'delete');
        form.append('postId', postId);
        await fetch(CTX + '/PostServlet', { method: 'POST', body: form });
        const el = document.getElementById(`post-${postId}`);
        if (el) el.remove();
    } catch (e) { console.error(e); }
}

async function submitComment(postId) {
    const input = document.getElementById(`comment-input-${postId}`);
    const content = input.value.trim();
    if (!content) return;
    try {
        const form = new FormData();
        form.append('action', 'comment');
        form.append('postId', postId);
        form.append('content', content);
        await fetch(CTX + '/PostServlet', { method: 'POST', body: form });
        input.value = '';
        const list = document.getElementById(`comments-list-${postId}`);
        const div = document.createElement('div');
        div.className = 'flex gap-3 mb-3';
        div.innerHTML = `
            <div class="w-8 h-8 rounded-full bg-primary flex items-center justify-center text-white font-bold text-xs flex-shrink-0">
                ${(CURRENT_USER.fullName || CURRENT_USER.username).charAt(0).toUpperCase()}
            </div>
            <div class="bg-white rounded-xl px-3 py-2 flex-1 shadow-sm">
                <p class="font-semibold text-xs text-gray-700 mb-0.5">${CURRENT_USER.fullName || CURRENT_USER.username}</p>
                <p class="text-sm text-gray-800">${content}</p>
            </div>`;
        list.appendChild(div);
    } catch (e) { console.error(e); }
}

function setupPostForm() {
    const form = document.getElementById('postForm');
    if (!form) return;
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const content = document.getElementById('postContent').value.trim();
        const imageUrl = document.getElementById('postImage').value.trim();
        if (!content) return;
        try {
            const formData = new FormData();
            formData.append('action', 'create');
            formData.append('content', content);
            if (imageUrl) formData.append('imageUrl', imageUrl);
            const res = await fetch(CTX + '/PostServlet', { method: 'POST', body: formData });
            if (res.ok) {
                document.getElementById('postContent').value = '';
                document.getElementById('postImage').value = '';
                loadPosts();
            }
        } catch (e) { console.error(e); }
    });
}
