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

        User sender = userDAO.findById(currentUserId);
        if (sender != null) {
            java.util.Map<String, Object> event = new java.util.HashMap<>();
            event.put("type", "NEW_MESSAGE");
            event.put("senderUsername", sender.getUsername());
            event.put("receiverUsername", receiver.getUsername());
            // Send to receiver
            com.ptit.socialchat.websocket.ChatWebSocket.sendToUser(receiver.getId(), event);
            // Send to sender (to sync multiple tabs)
            com.ptit.socialchat.websocket.ChatWebSocket.sendToUser(currentUserId, event);
        }
    }

    public boolean isFriendWith(long currentUserId, String otherUsername) {
        User otherUser = userDAO.findByUsername(otherUsername.trim());
        if (otherUser == null) return false;
        return friendDAO.isFriend(currentUserId, otherUser.getId());
    }

    public void markMessagesAsRead(long currentUserId, String senderUsername) {
        if (senderUsername == null || senderUsername.trim().isEmpty()) return;
        User sender = userDAO.findByUsername(senderUsername.trim());
        if (sender != null) {
            messageDAO.markMessagesAsRead(sender.getId(), currentUserId);
            
            User current = userDAO.findById(currentUserId);
            if (current != null) {
                java.util.Map<String, Object> event = new java.util.HashMap<>();
                event.put("type", "MESSAGES_READ");
                event.put("readerUsername", current.getUsername());
                com.ptit.socialchat.websocket.ChatWebSocket.sendToUser(sender.getId(), event);
            }
        }
    }

    public java.util.Map<String, Long> getUnreadCounts(long currentUserId) {
        java.util.Map<Long, Long> countsById = messageDAO.getUnreadCounts(currentUserId);
        java.util.Map<String, Long> countsByUsername = new java.util.HashMap<>();
        for (java.util.Map.Entry<Long, Long> entry : countsById.entrySet()) {
            User sender = userDAO.findById(entry.getKey());
            if (sender != null) {
                countsByUsername.put(sender.getUsername(), entry.getValue());
            }
        }
        return countsByUsername;
    }

    public long getTotalUnreadConversations(long currentUserId) {
        return messageDAO.getTotalUnreadConversations(currentUserId);
    }

    public long getLastReadMessageId(long currentUserId, String otherUsername) {
        User otherUser = userDAO.findByUsername(otherUsername.trim());
        if (otherUser != null) {
            return messageDAO.getLastReadMessageId(currentUserId, otherUser.getId());
        }
        return 0;
    }

    public void clearChatHistory(long currentUserId, String otherUsername) {
        User otherUser = userDAO.findByUsername(otherUsername.trim());
        if (otherUser != null) {
            messageDAO.clearChatHistory(currentUserId, otherUser.getId());
        }
    }

    public void recallMessage(long messageId, long currentUserId) {
        Message msg = messageDAO.findById(messageId);
        if (msg == null) return;
        
        // Security check: Only sender can recall
        if (msg.getSender().getId() != currentUserId) return;
        
        messageDAO.recallMessage(messageId, currentUserId);
        
        // Notify partner via WebSocket
        long receiverId = msg.getReceiver().getId();
        java.util.Map<String, Object> event = new java.util.HashMap<>();
        event.put("type", "MESSAGE_RECALLED");
        event.put("messageId", messageId);
        event.put("partnerUsername", msg.getSender().getUsername());
        
        com.ptit.socialchat.websocket.ChatWebSocket.sendToUser(receiverId, event);
        
        // Sync to sender's other tabs
        java.util.Map<String, Object> eventSelf = new java.util.HashMap<>();
        eventSelf.put("type", "MESSAGE_RECALLED");
        eventSelf.put("messageId", messageId);
        eventSelf.put("partnerUsername", msg.getReceiver().getUsername());
        com.ptit.socialchat.websocket.ChatWebSocket.sendToUser(currentUserId, eventSelf);
    }
}
