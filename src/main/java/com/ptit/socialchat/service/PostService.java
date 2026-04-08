package com.ptit.socialchat.service;

import com.ptit.socialchat.dao.FriendDAO;
import com.ptit.socialchat.dao.PostDAO;
import com.ptit.socialchat.model.Post;
import com.ptit.socialchat.util.ValidationUtil;

public class PostService {

    private final PostDAO postDAO = new PostDAO();
    private final FriendDAO friendDAO = new FriendDAO();

    public void createPost(long userId, String content, String imageUrl) throws IllegalArgumentException {
        boolean hasContent = content != null && !content.trim().isEmpty();
        boolean hasImage = imageUrl != null && !imageUrl.trim().isEmpty();

        if (!hasContent && !hasImage) {
            throw new IllegalArgumentException("Bài viết phải có nội dung hoặc hình ảnh.");
        }

        if (hasContent && content.trim().length() > 5000) {
            throw new IllegalArgumentException("Nội dung bài viết tối đa 5000 ký tự.");
        }
        
        postDAO.save(content != null ? content.trim() : "", hasImage ? imageUrl.trim() : null, userId);
    }

    public void addComment(long userId, long postId, String content) throws IllegalArgumentException {
        if (!ValidationUtil.isValidCommentContent(content)) {
            throw new IllegalArgumentException("Bình luận không được để trống và tối đa 500 ký tự.");
        }

        Post post = postDAO.findById(postId);
        if (post == null) {
            throw new IllegalArgumentException("Không tìm thấy bài viết này. Có thể bài viết đã bị xóa.");
        }

        // BẢO MẬT: Chỉ cho bình luận nếu là chủ bài viết hoặc là Bạn Bè
        if (userId != post.getUser().getId()) {
            if (!friendDAO.isFriend(userId, post.getUser().getId())) {
                throw new IllegalArgumentException("Bảo mật: Chỉ bạn bè mới có thể bình luận vào bài viết này.");
            }
        }

        postDAO.addComment(postId, content.trim(), userId);
    }

    public boolean toggleLike(long userId, long postId) throws IllegalArgumentException {
        Post post = postDAO.findById(postId);
        if (post == null) {
            throw new IllegalArgumentException("Không tìm thấy bài viết này.");
        }

        return postDAO.toggleLike(postId, userId);
    }

    public void deletePost(long userId, long postId, String userRole) throws IllegalArgumentException {
        Post post = postDAO.findById(postId);
        if (post == null) {
            throw new IllegalArgumentException("Không tìm thấy bài viết này.");
        }

        if (post.getUser().getId() != userId && !"ROLE_ADMIN".equals(userRole)) {
            throw new IllegalArgumentException("Bạn không có quyền xóa bài viết của người khác.");
        }

        postDAO.delete(postId);
    }
}
