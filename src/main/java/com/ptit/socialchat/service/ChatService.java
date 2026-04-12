package com.ptit.socialchat.service;

import com.ptit.socialchat.dao.FriendDAO;
import com.ptit.socialchat.dao.MessageDAO;
import com.ptit.socialchat.dao.UserDAO;
import com.ptit.socialchat.model.Message;
import com.ptit.socialchat.model.User;
import com.ptit.socialchat.util.ValidationUtil;

import java.util.List;

public class ChatService {

    private final MessageDAO messageDAO = new MessageDAO();
    private final FriendDAO friendDAO = new FriendDAO();
    private final UserDAO userDAO = new UserDAO();

    public List<Message> getChatHistory(long currentUserId, String otherUsername, long lastMessageId, int limit) throws IllegalArgumentException {
        if (otherUsername == null || otherUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng cung cấp tên người trò chuyện hợp lệ.");
        }

        User otherUser = userDAO.findByUsername(otherUsername.trim());
        if (otherUser == null) {
            throw new IllegalArgumentException("Không tìm thấy thông tin đối phương.");
        }

        return messageDAO.getChatHistory(currentUserId, otherUser.getId(), lastMessageId, limit);
    }

    public void sendMessage(long currentUserId, String receiverUsername, String content, String imageUrl) throws IllegalArgumentException {
        boolean hasContent = content != null && !content.trim().isEmpty();
        boolean hasImage = imageUrl != null && !imageUrl.trim().isEmpty();

        if (!hasContent && !hasImage) {
            throw new IllegalArgumentException("Tin nhắn hoặc ảnh không được để trống.");
        }
        if (hasContent && content.trim().length() > 2000) {
            throw new IllegalArgumentException("Tin nhắn tối đa 2000 ký tự.");
        }

        if (receiverUsername == null || receiverUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng cung cấp tên người nhận.");
        }

        User receiver = userDAO.findByUsername(receiverUsername.trim());
        if (receiver == null) {
            throw new IllegalArgumentException("Không tìm thấy thông tin người nhận.");
        }

        if (currentUserId == receiver.getId()) {
            throw new IllegalArgumentException("Bạn không thể tự nhắn tin cho chính mình.");
        }

        if (!friendDAO.isFriend(currentUserId, receiver.getId())) {
            throw new IllegalArgumentException("Bảo mật: Phải kết bạn mới có thể bắt đầu cuộc trò chuyện.");
        }

        messageDAO.save(currentUserId, receiver.getId(), content != null ? content.trim() : "", imageUrl);
    }

    public boolean isFriendWith(long currentUserId, String otherUsername) {
        User otherUser = userDAO.findByUsername(otherUsername.trim());
        if (otherUser == null) return false;
        return friendDAO.isFriend(currentUserId, otherUser.getId());
    }
}
