package com.ptit.socialchat.service;

import com.ptit.socialchat.dao.FriendDAO;
import com.ptit.socialchat.dao.ModerationSettingsDAO;
import com.ptit.socialchat.dao.PostDAO;
import com.ptit.socialchat.model.ModerationSettings;
import com.ptit.socialchat.model.Post;
import com.ptit.socialchat.util.ValidationUtil;

public class PostService {

    private final PostDAO postDAO = new PostDAO();
    private final FriendDAO friendDAO = new FriendDAO();
    private final ModerationSettingsDAO moderationSettingsDAO = new ModerationSettingsDAO();
    private final ModerationService moderationService = new ModerationService();

    /**
     * Kết quả tạo bài viết — cho frontend biết trạng thái.
     */
    public static class CreatePostResult {
        private String status;   // APPROVED, PENDING, REJECTED
        private String message;  // Thông báo cho user
        private String label;    // Nhãn AI (nếu có)

        public CreatePostResult(String status, String message, String label) {
            this.status = status;
            this.message = message;
            this.label = label;
        }

        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public String getLabel() { return label; }
    }

    /**
     * Tạo bài viết mới — tự động áp dụng chế độ kiểm duyệt hiện tại.
     */
    public CreatePostResult createPost(long userId, String content, String imageUrl) throws IllegalArgumentException {
        boolean hasContent = content != null && !content.trim().isEmpty();
        boolean hasImage = imageUrl != null && !imageUrl.trim().isEmpty();

        if (!hasContent && !hasImage) {
            throw new IllegalArgumentException("Bài viết phải có nội dung hoặc hình ảnh.");
        }

        if (hasContent && content.trim().length() > 5000) {
            throw new IllegalArgumentException("Nội dung bài viết tối đa 5000 ký tự.");
        }

        String trimmedContent = content != null ? content.trim() : "";
        String trimmedImage = hasImage ? imageUrl.trim() : null;

        // Lấy cấu hình kiểm duyệt
        ModerationSettings settings = moderationSettingsDAO.getSettings();
        String mode = settings.getMode();

        System.out.println("[PostService] Creating post with moderation mode: " + mode);

        switch (mode) {
            case "MANUAL":
                // Chế độ duyệt thủ công — chờ admin duyệt
                postDAO.save(trimmedContent, trimmedImage, userId, "PENDING", null, null);
                return new CreatePostResult("PENDING",
                        "Bài viết đã được gửi và đang chờ kiểm duyệt.", null);

            case "AUTO_AI":
                // Chế độ tự động AI
                return handleAutoAIModeration(trimmedContent, trimmedImage, userId, settings.getAiServiceUrl());

            case "NONE":
            default:
                // Không kiểm duyệt — đăng ngay
                postDAO.save(trimmedContent, trimmedImage, userId, "APPROVED", null, null);
                return new CreatePostResult("APPROVED",
                        "Đăng bài thành công!", null);
        }
    }

    /**
     * Xử lý kiểm duyệt tự động bằng AI.
     */
    private CreatePostResult handleAutoAIModeration(String content, String imageUrl,
                                                     long userId, String aiServiceUrl) {
        // Nếu chỉ có ảnh không có text → không kiểm duyệt text, cho qua
        if (content == null || content.isEmpty()) {
            postDAO.save(content, imageUrl, userId, "APPROVED", "CLEAN", 1.0);
            return new CreatePostResult("APPROVED", "Đăng bài thành công!", "CLEAN");
        }

        // Gọi AI Service
        ModerationService.ModerationResult aiResult = moderationService.moderate(content, aiServiceUrl);

        if (!aiResult.isSuccess()) {
            // AI service lỗi → fallback: cho vào hàng chờ duyệt thủ công
            System.err.println("[PostService] AI service failed: " + aiResult.getErrorMessage());
            postDAO.save(content, imageUrl, userId, "PENDING", null, null);
            return new CreatePostResult("PENDING",
                    "AI Moderation tạm thời không khả dụng. Bài viết đang chờ kiểm duyệt thủ công.",
                    null);
        }

        if (aiResult.isToxic()) {
            // Nội dung toxic → TỰ ĐỘNG TỪ CHỐI
            postDAO.save(content, imageUrl, userId, "REJECTED",
                    aiResult.getLabel(), aiResult.getConfidence());
            String labelVi = "OFFENSIVE".equals(aiResult.getLabel())
                    ? "Xúc phạm" : "Thù ghét";
            return new CreatePostResult("REJECTED",
                    "Bài viết bị từ chối tự động. Nội dung được phát hiện: " + labelVi
                            + " (độ tin cậy: " + Math.round(aiResult.getConfidence() * 100) + "%).",
                    aiResult.getLabel());
        } else {
            // Nội dung sạch → TỰ ĐỘNG DUYỆT
            postDAO.save(content, imageUrl, userId, "APPROVED",
                    aiResult.getLabel(), aiResult.getConfidence());
            return new CreatePostResult("APPROVED", "Đăng bài thành công!", aiResult.getLabel());
        }
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
