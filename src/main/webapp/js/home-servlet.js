// home-servlet.js - dùng với home.jsp, gọi /PostServlet thay vì /api/posts
// v3.0 — search, hashtags, announcements, topics

document.addEventListener('DOMContentLoaded', function() {
    loadPosts();
    setupPostForm();
    loadAnnouncements();
});

// ═══════════════════════════════════════════════════
//  SEARCH
// ═══════════════════════════════════════════════════
var currentSearchQuery = '';

function searchPosts() {
    var input = document.getElementById('searchInput');
    var query = input ? input.value.trim() : '';
    if (!query) return;
    currentSearchQuery = query;
    document.getElementById('searchIndicator').classList.remove('hidden');
    document.getElementById('searchQueryText').textContent = query;
    document.getElementById('clearSearchBtn').classList.remove('hidden');
    loadPosts(query);
}

function clearSearch() {
    currentSearchQuery = '';
    var input = document.getElementById('searchInput');
    if (input) input.value = '';
    document.getElementById('searchIndicator').classList.add('hidden');
    document.getElementById('clearSearchBtn').classList.add('hidden');
    loadPosts();
}

// ═══════════════════════════════════════════════════
//  MODERATION NOTICE
// ═══════════════════════════════════════════════════
function showModerationNotice(type, message) {
    var feed = document.getElementById('postsFeed');
    if (!feed) return;

    var bgColor = type === 'rejected' ? 'bg-red-50 border-red-200' : 'bg-yellow-50 border-yellow-200';
    var iconColor = type === 'rejected' ? 'text-red-500' : 'text-yellow-500';
    var icon = type === 'rejected' ? 'fa-exclamation-triangle' : 'fa-hourglass-half';
    var textColor = type === 'rejected' ? 'text-red-700' : 'text-yellow-700';

    var notice = document.createElement('div');
    notice.className = bgColor + ' border rounded-2xl p-4 mb-4 flex items-start gap-3';
    notice.style.animation = 'fadeIn 0.3s ease';
    notice.innerHTML = '<i class="fas ' + icon + ' ' + iconColor + ' text-lg mt-0.5"></i>'
        + '<div class="flex-1">'
        + '<p class="' + textColor + ' text-sm font-medium">' + message + '</p>'
        + '</div>'
        + '<button onclick="this.parentElement.remove()" class="text-gray-400 hover:text-gray-600 text-sm"><i class="fas fa-times"></i></button>';

    feed.insertBefore(notice, feed.firstChild);
    setTimeout(function() { if (notice.parentNode) notice.remove(); }, 8000);
}

// ═══════════════════════════════════════════════════
//  HELPERS
// ═══════════════════════════════════════════════════
function escapeHtml(unsafe) {
    if (!unsafe) return "";
    return String(unsafe)
         .replace(/&/g, "&amp;")
         .replace(/</g, "&lt;")
         .replace(/>/g, "&gt;")
         .replace(/"/g, "&quot;")
         .replace(/'/g, "&#039;");
}

function formatTime(dateStr) {
    if (!dateStr) return '';
    var now = new Date();
    var d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;
    var diffMs = now - d;
    var diffMin = Math.floor(diffMs / 60000);
    var diffHour = Math.floor(diffMin / 60);
    var diffDay = Math.floor(diffHour / 24);
    if (diffMin < 1) return 'Vừa xong';
    if (diffMin < 60) return diffMin + ' phút trước';
    if (diffHour < 24) return diffHour + ' giờ trước';
    if (diffDay < 30) return diffDay + ' ngày trước';
    return d.toLocaleDateString('vi-VN');
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    var d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;
    return d.toLocaleDateString('vi-VN');
}

/**
 * Render nội dung bài viết: escape HTML, sau đó highlight #hashtags.
 * Mỗi hashtag có màu riêng biệt.
 */
var TAG_COLORS = {
    '#just-for-fun': 'hashtag-blue',
    '#quan-trọng':   'hashtag-red',
    '#hỏi-đáp':     'hashtag-green',
    '#chia-sẻ':     'hashtag-purple',
    '#học-tập':     'hashtag-yellow'
};

function renderContent(content) {
    if (!content) return '';
    var escaped = escapeHtml(content);
    // Replace #hashtag patterns with styled spans
    escaped = escaped.replace(/(#[\w\u00C0-\u024F\u1E00-\u1EFF-]+)/g, function(match) {
        var lower = match.toLowerCase();
        var colorClass = TAG_COLORS[lower] || 'hashtag-default';
        return '<span class="hashtag-tag ' + colorClass + '">' + match + '</span>';
    });
    return escaped;
}

// ═══════════════════════════════════════════════════
//  LOAD POSTS
// ═══════════════════════════════════════════════════
var allPostsData = []; // store for topic extraction

async function loadPosts(searchQuery) {
    try {
        var url = CTX + '/PostServlet';
        var params = [];
        if (typeof PROFILE_USERNAME !== 'undefined' && PROFILE_USERNAME) {
            params.push('username=' + encodeURIComponent(PROFILE_USERNAME));
        }
        if (searchQuery) {
            params.push('search=' + encodeURIComponent(searchQuery));
        }
        if (params.length) url += '?' + params.join('&');

        var posts = await apiFetch(url);
        var feed = document.getElementById('postsFeed');
        if (!feed) return;
        feed.innerHTML = '';
        allPostsData = posts || [];

        if (!posts || !posts.length) {
            feed.innerHTML = '<div class="bg-white rounded-2xl shadow-sm p-8 text-center text-gray-400 text-sm">'
                + (searchQuery ? 'Không tìm thấy bài viết nào phù hợp.' : 'Chưa có bài viết nào.')
                + '</div>';
        } else {
            posts.forEach(function(post) { renderPost(post, feed); });
        }

        // Extract and render topics from all loaded posts
        extractTopics(allPostsData);
    } catch (e) { console.error('loadPosts error:', e); }
}

// ═══════════════════════════════════════════════════
//  RENDER POST
// ═══════════════════════════════════════════════════
function renderPost(post, container) {
    var initials = (post.userFullName || post.username || '?').charAt(0).toUpperCase();
    var isOwner = post.username === CURRENT_USER.username;
    var isAdmin = CURRENT_USER.role === 'ROLE_ADMIN';
    var liked = post.liked;
    var likeCount = post.likeCount || 0;
    var commentCount = (post.comments || []).length;

    var avatarHtml;
    if (post.avatar) {
        avatarHtml = '<img src="' + post.avatar + '" class="w-10 h-10 rounded-full object-cover flex-shrink-0" onerror="this.style.display=\'none\'">';
    } else {
        avatarHtml = '<div class="w-10 h-10 rounded-full bg-primary flex items-center justify-center text-white font-bold text-sm flex-shrink-0">' + initials + '</div>';
    }

    // Build comments HTML
    var commentsHtml = '';
    (post.comments || []).forEach(function(c) {
        var cInit = (c.userFullName || c.username || 'U').charAt(0).toUpperCase();
        var cFullNameOrUsername = escapeHtml(c.userFullName || c.username);
        var cAvt;
        if (c.avatar) {
            cAvt = '<img src="' + escapeHtml(c.avatar) + '" class="w-8 h-8 rounded-full object-cover flex-shrink-0" onerror="this.style.display=\'none\'">';
        } else {
            cAvt = '<div class="w-8 h-8 rounded-full bg-primary flex items-center justify-center text-white font-bold text-xs flex-shrink-0">' + cInit + '</div>';
        }
        commentsHtml += '<div class="flex gap-3 mb-3">'
            + '<a href="' + CTX + '/ProfileServlet?username=' + escapeHtml(c.username) + '" class="flex-shrink-0 hover:opacity-80 transition">' + cAvt + '</a>'
            + '<div class="bg-white rounded-xl px-3 py-2 flex-1 shadow-sm">'
            + '<p class="font-semibold text-xs text-gray-700 mb-0.5"><a href="' + CTX + '/ProfileServlet?username=' + escapeHtml(c.username) + '" class="hover:underline">' + cFullNameOrUsername + '</a></p>'
            + '<p class="text-sm text-gray-800" style="white-space: pre-wrap;">' + escapeHtml(c.content) + '</p>'
            + '</div></div>';
    });

    var deleteBtn = (isOwner || isAdmin)
        ? '<button onclick="deletePost(' + post.id + ')" class="text-gray-300 hover:text-red-500 transition text-sm px-2"><i class="fas fa-trash"></i></button>'
        : '';

    var imageHtml = post.imageUrl
        ? '<img src="' + post.imageUrl + '" alt="Post image" class="w-full rounded-xl mb-3 max-h-96 object-cover" onerror="this.style.display=\'none\'">'
        : '';

    var div = document.createElement('div');
    div.id = 'post-' + post.id;
    div.className = 'bg-white rounded-2xl shadow-sm mb-4 overflow-hidden';
    var canComment = post.isFriend || isOwner || isAdmin;
    var commentInputHtml = canComment ? 
      '<div class="flex gap-2 mt-2">'
    + '<input type="text" id="comment-input-' + post.id + '" placeholder="Viết bình luận..."'
    + ' class="flex-1 bg-white border border-gray-200 rounded-full px-4 py-2 text-sm focus:outline-none focus:border-primary transition"'
    + ' onkeyup="if(event.key===\'Enter\') submitComment(' + post.id + ', this)">'
    + '<button onclick="submitComment(' + post.id + ', document.getElementById(\'comment-input-' + post.id + '\'))" class="bg-primary hover:bg-primary-dark text-white text-xs font-semibold px-4 py-2 rounded-full transition">Gửi</button>'
    + '</div>'
    : '<p class="text-xs text-gray-400 italic text-center mt-3 mb-1">Chỉ bạn bè mới có thể bình luận.</p>';

    div.innerHTML = '<div class="p-5">'
        + '<div class="flex items-center gap-3 mb-3">'
        + '<a href="' + CTX + '/ProfileServlet?username=' + escapeHtml(post.username) + '" class="flex-shrink-0 hover:opacity-80 transition">' + avatarHtml + '</a>'
        + '<div class="flex-1 min-w-0">'
        + '<p class="font-semibold text-gray-900 text-sm"><a href="' + CTX + '/ProfileServlet?username=' + escapeHtml(post.username) + '" class="hover:underline">' + escapeHtml(post.userFullName || post.username) + '</a></p>'
        + '<p class="text-xs text-gray-400">' + formatTime(post.createdAt) + '</p>'
        + '</div>'
        + deleteBtn
        + '</div>'
        + '<p class="text-gray-800 text-sm leading-relaxed mb-3" style="white-space: pre-wrap;">' + renderContent(post.content || '') + '</p>'
        + imageHtml
        + '<div class="flex items-center gap-4 text-xs text-gray-400 mb-3">'
        + '<span id="like-count-' + post.id + '">' + likeCount + ' lượt thích</span>'
        + '<span id="comment-count-' + post.id + '">' + commentCount + ' bình luận</span>'
        + '</div>'
        + '</div>'
        + '<div class="border-t border-gray-100"></div>'
        + '<div class="flex px-2 py-1">'
        + '<button onclick="toggleLike(' + post.id + ', this)" class="flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl text-sm font-medium transition hover:bg-gray-50 ' + (liked ? 'text-primary' : 'text-gray-500') + '" data-liked="' + liked + '">'
        + '<i class="' + (liked ? 'fas' : 'far') + ' fa-heart"></i> Thích'
        + '</button>'
        + '<button onclick="toggleComments(' + post.id + ')" class="flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl text-sm font-medium text-gray-500 hover:bg-gray-50 transition">'
        + '<i class="far fa-comment"></i> Bình luận'
        + '</button>'
        + '</div>'
        + '<div id="comments-' + post.id + '" class="hidden border-t border-gray-100 p-4 bg-gray-50">'
        + '<div id="comments-list-' + post.id + '">'
        + commentsHtml
        + '</div>'
        + commentInputHtml
        + '</div>';
    container.appendChild(div);
}

// ═══════════════════════════════════════════════════
//  COMMENTS / LIKES / DELETE
// ═══════════════════════════════════════════════════
function toggleComments(postId) {
    document.getElementById('comments-' + postId).classList.toggle('hidden');
}

async function toggleLike(postId, btn) {
    try {
        var params = new URLSearchParams();
        params.append('action', 'like');
        params.append('postId', postId);
        var data = await apiFetch(CTX + '/PostServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        });

        if (!data) return;

        var liked = data.liked;
        btn.dataset.liked = liked;
        if (liked) {
            btn.classList.remove('text-gray-500');
            btn.classList.add('text-primary');
        } else {
            btn.classList.remove('text-primary');
            btn.classList.add('text-gray-500');
        }
        btn.innerHTML = '<i class="' + (liked ? 'fas' : 'far') + ' fa-heart"></i> Thích';
        var countEl = document.getElementById('like-count-' + postId);
        if (countEl) countEl.textContent = data.likeCount + ' lượt thích';
    } catch (e) { console.error(e); }
}

async function deletePost(postId) {
    if (!confirm('Xóa bài viết này?')) return;
    try {
        var params = new URLSearchParams();
        params.append('action', 'delete');
        params.append('postId', postId);
        await apiFetch(CTX + '/PostServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        }, false);
        var el = document.getElementById('post-' + postId);
        if (el) el.remove();
    } catch (e) { console.error(e); }
}

function submitComment(postId, inputEl) {
    var content = inputEl.value.trim();
    if (!content) return;

    var params = new URLSearchParams();
    params.append('action', 'comment');
    params.append('postId', postId);
    params.append('content', content);

    var submitBtn = inputEl.nextElementSibling;
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '...';
    }

    apiFetch(CTX + '/PostServlet', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    }, false).then(function() {
        inputEl.value = '';
        var list = document.getElementById('comments-list-' + postId);
        if (!list) return;

        var div = document.createElement('div');
        div.className = 'flex gap-3 mb-3';
        var myInit = (CURRENT_USER.fullName || CURRENT_USER.username).charAt(0).toUpperCase();
        var myAvt;
        if (CURRENT_USER.avatar) {
            myAvt = '<img src="' + CURRENT_USER.avatar + '" class="w-8 h-8 rounded-full object-cover flex-shrink-0" onerror="this.style.display=\'none\'">';
        } else {
            myAvt = '<div class="w-8 h-8 rounded-full bg-primary flex items-center justify-center text-white font-bold text-xs flex-shrink-0">' + myInit + '</div>';
        }

        div.innerHTML = myAvt
            + '<div class="bg-white rounded-xl px-3 py-2 flex-1 shadow-sm border border-gray-100">'
            + '<p class="font-semibold text-xs text-gray-700 mb-0.5">' + escapeHtml(CURRENT_USER.fullName || CURRENT_USER.username) + '</p>'
            + '<p class="text-sm text-gray-800" style="white-space: pre-wrap;">' + escapeHtml(content) + '</p>'
            + '</div>';
        list.appendChild(div);

        var countSpan = document.getElementById('comment-count-' + postId);
        if (countSpan) {
            var currentCount = parseInt(countSpan.textContent) || 0;
            countSpan.textContent = (currentCount + 1) + ' bình luận';
        }
    }).catch(function(err) {
        console.error('Comment error:', err);
    }).finally(function() {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = 'Gửi';
        }
    });
}

// ═══════════════════════════════════════════════════
//  POST FORM
// ═══════════════════════════════════════════════════
function setupPostForm() {
    var form = document.getElementById('postForm');
    var imageInput = document.getElementById('postImageFile');
    var previewContainer = document.getElementById('createPostImagePreviewContainer');
    var previewImg = document.getElementById('createPostImagePreview');
    var cancelPreviewBtn = document.getElementById('cancelPostImagePreview');
    var submitBtn = document.getElementById('postSubmitBtn');

    if (!form) return;

    if (imageInput && previewContainer) {
        imageInput.addEventListener('change', function() {
            if (this.files && this.files[0]) {
                var reader = new FileReader();
                reader.onload = function(e) {
                    previewImg.src = e.target.result;
                    previewContainer.classList.remove('hidden');
                };
                reader.readAsDataURL(this.files[0]);
            }
        });

        cancelPreviewBtn.addEventListener('click', function() {
            imageInput.value = '';
            previewContainer.classList.add('hidden');
            previewImg.src = '';
        });
    }

    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        var content = document.getElementById('postContent').value.trim();
        var hasImage = imageInput && imageInput.files && imageInput.files.length > 0;

        if (!content && !hasImage) return;

        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang đăng...';
        }

        try {
            var selectedImageUrl = '';
            if (hasImage) {
                var formData = new FormData();
                formData.append('imageFile', imageInput.files[0]);
                var uploadResult = await apiFetch(CTX + '/UploadChatImage', {
                    method: 'POST',
                    body: formData
                });
                if (uploadResult.imageUrl) {
                    selectedImageUrl = uploadResult.imageUrl;
                } else {
                    alert('Lỗi tải lên ảnh');
                    if (submitBtn) {
                        submitBtn.disabled = false;
                        submitBtn.innerHTML = '<i class="fas fa-paper-plane"></i> Đăng';
                    }
                    return;
                }
            }

            var params = new URLSearchParams();
            params.append('action', 'create');
            params.append('content', content);
            if (selectedImageUrl) params.append('imageUrl', selectedImageUrl);

            var result = await apiFetch(CTX + '/PostServlet', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params.toString()
            });

            document.getElementById('postContent').value = '';
            if (cancelPreviewBtn) cancelPreviewBtn.click();

            // Xử lý kết quả kiểm duyệt
            if (result && result.status === 'PENDING') {
                showModerationNotice('pending', result.message || 'Bài viết đang chờ kiểm duyệt.');
            } else if (result && result.status === 'REJECTED') {
                showModerationNotice('rejected', result.message || 'Bài viết bị từ chối do nội dung không phù hợp.');
            } else {
                loadPosts();
            }
        } catch (err) {
            console.error('Create post error:', err);
        } finally {
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<i class="fas fa-paper-plane"></i> Đăng';
            }
        }
    });
}

// ═══════════════════════════════════════════════════
//  ANNOUNCEMENTS SIDEBAR
// ═══════════════════════════════════════════════════
async function loadAnnouncements() {
    var box = document.getElementById('announcementsBox');
    if (!box) return;
    try {
        var data = await apiFetch(CTX + '/PostServlet?type=announcements');
        if (!data || !data.length) {
            box.innerHTML = '<p class="text-xs text-gray-400 text-center py-4">Chưa có thông báo nào.</p>';
            return;
        }
        box.innerHTML = '';
        data.forEach(function(ann) {
            var div = document.createElement('div');
            div.className = 'border-b border-gray-100 py-3 px-1 last:border-0 hover:bg-gray-50 transition rounded-lg cursor-default';
            div.innerHTML = '<div class="flex items-center gap-2 mb-1">'
                + '<span class="text-xs text-gray-400">' + formatDate(ann.createdAt) + '</span>'
                + '</div>'
                + '<p class="text-sm font-semibold text-gray-900 mb-0.5">' + escapeHtml(ann.title) + '</p>'
                + '<p class="text-xs text-gray-500 line-clamp-2">' + escapeHtml(ann.content) + '</p>';
            box.appendChild(div);
        });
    } catch (e) {
        box.innerHTML = '<p class="text-xs text-gray-400 text-center py-4">Lỗi tải thông báo.</p>';
    }
}

// ═══════════════════════════════════════════════════
//  TOPICS SIDEBAR (extract hashtags from posts)
// ═══════════════════════════════════════════════════
function extractTopics(posts) {
    var box = document.getElementById('topicsBox');
    if (!box) return;

    var tagCounts = {};
    (posts || []).forEach(function(post) {
        if (!post.content) return;
        var matches = post.content.match(/#[\w\u00C0-\u024F\u1E00-\u1EFF-]+/g);
        if (matches) {
            matches.forEach(function(tag) {
                var lower = tag.toLowerCase();
                tagCounts[lower] = (tagCounts[lower] || 0) + 1;
            });
        }
    });

    // Sort by count desc
    var sorted = Object.keys(tagCounts).sort(function(a, b) { return tagCounts[b] - tagCounts[a]; });

    if (sorted.length === 0) {
        box.innerHTML = '<p class="text-xs text-gray-400 text-center py-4 w-full">Chưa có chủ đề nào.</p>';
        return;
    }

    box.innerHTML = '';
    // Color palette for topic tags
    var colors = [
        'bg-blue-50 text-blue-600 hover:bg-blue-100',
        'bg-green-50 text-green-600 hover:bg-green-100',
        'bg-purple-50 text-purple-600 hover:bg-purple-100',
        'bg-orange-50 text-orange-600 hover:bg-orange-100',
        'bg-pink-50 text-pink-600 hover:bg-pink-100',
        'bg-cyan-50 text-cyan-600 hover:bg-cyan-100',
        'bg-yellow-50 text-yellow-700 hover:bg-yellow-100',
        'bg-red-50 text-red-500 hover:bg-red-100'
    ];

    sorted.slice(0, 15).forEach(function(tag, idx) {
        var colorClass = colors[idx % colors.length];
        var btn = document.createElement('button');
        btn.className = 'text-xs font-medium px-3 py-1.5 rounded-full transition cursor-pointer ' + colorClass;
        btn.textContent = tag + ' (' + tagCounts[tag] + ')';
        btn.onclick = function() {
            var input = document.getElementById('searchInput');
            if (input) input.value = tag;
            searchPosts();
        };
        box.appendChild(btn);
    });
}
