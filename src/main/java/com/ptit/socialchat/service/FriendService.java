package com.ptit.socialchat.service;

import com.ptit.socialchat.dao.FriendDAO;
import com.ptit.socialchat.dao.UserDAO;
import com.ptit.socialchat.model.FriendRequest;
import com.ptit.socialchat.model.User;
import java.util.List;

public class FriendService {

    private final FriendDAO friendDAO = new FriendDAO();
    private final UserDAO userDAO = new UserDAO();

    public void sendFriendRequest(long senderId, String receiverUsername) throws IllegalArgumentException {
        if (receiverUsername == null || receiverUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng cung cấp tên người nhận.");
        }

        User receiver = userDAO.findByUsername(receiverUsername.trim());
        if (receiver == null) {
            throw new IllegalArgumentException("Người dùng không tồn tại.");
        }

        if (receiver.getId() == senderId) {
            throw new IllegalArgumentException("Bạn không thể gửi lời mời kết bạn cho chính mình.");
        }

        if (friendDAO.isFriend(senderId, receiver.getId())) {
            throw new IllegalArgumentException("Bạn và người này đã là bạn bè.");
        }

        if (friendDAO.hasPendingRequest(senderId, receiver.getId())) {
            throw new IllegalArgumentException("Đã tồn tại lời mời kết bạn đang chờ xử lý giữa hai người.");
        }

        friendDAO.sendRequest(senderId, receiver.getId());
    }

    public void acceptFriendRequest(long requestId, long currentUserId) throws IllegalArgumentException {
        FriendRequest fr = friendDAO.findRequestById(requestId);
        if (fr == null) {
            throw new IllegalArgumentException("Không tìm thấy lời mời kết bạn.");
        }

        // BẢO MẬT: Phải là người NHẬN (Receiver) mới được quyền Accept.
        if (fr.getReceiver().getId() != currentUserId) {
            throw new IllegalArgumentException("Bạn không có quyền chấp nhận lời mời của người khác.");
        }

        if (!"PENDING".equalsIgnoreCase(fr.getStatus())) {
            throw new IllegalArgumentException("Lời mời kết bạn đã được xử lý từ trước.");
        }

        // Tránh Add 2 lần nếu do cache hoặc request dính lỗi
        if (!friendDAO.isFriend(fr.getSender().getId(), currentUserId)) {
            friendDAO.updateRequestStatus(requestId, "ACCEPTED");
            friendDAO.addFriendship(fr.getSender().getId(), currentUserId);
        } else {
            friendDAO.updateRequestStatus(requestId, "ACCEPTED");
        }
    }

    public void acceptFriendRequestByUsername(long currentUserId, String senderUsername) throws IllegalArgumentException {
        if (senderUsername == null || senderUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng cung cấp tên người gửi.");
        }
        User sender = userDAO.findByUsername(senderUsername.trim());
        if (sender == null) throw new IllegalArgumentException("Người dùng không tồn tại.");

        List<FriendRequest> requests = friendDAO.getPendingRequests(currentUserId);
        FriendRequest fr = requests.stream()
                .filter(r -> r.getSender().getId() == sender.getId())
                .findFirst()
                .orElse(null);

        if (fr == null) {
            throw new IllegalArgumentException("Không tìm thấy lời mời kết bạn từ người này.");
        }

        acceptFriendRequest(fr.getId(), currentUserId);
    }

    public void rejectFriendRequest(long requestId, long currentUserId) throws IllegalArgumentException {
        FriendRequest fr = friendDAO.findRequestById(requestId);
        if (fr == null) {
            throw new IllegalArgumentException("Không tìm thấy lời mời kết bạn.");
        }

        // BẢO MẬT: Phải là người NHẬN (Receiver) mới được quyền Reject.
        if (fr.getReceiver().getId() != currentUserId) {
            throw new IllegalArgumentException("Bạn không có quyền từ chối lời mời của người khác.");
        }

        if (!"PENDING".equalsIgnoreCase(fr.getStatus())) {
            throw new IllegalArgumentException("Lời mời kết bạn đã được xử lý từ trước.");
        }

        friendDAO.updateRequestStatus(requestId, "REJECTED");
    }

    public void unfriend(long currentUserId, String targetUsername) throws IllegalArgumentException {
        if (targetUsername == null || targetUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng cung cấp tên người dùng.");
        }
        User target = userDAO.findByUsername(targetUsername.trim());
        if (target == null) throw new IllegalArgumentException("Người dùng không tồn tại.");

        if (!friendDAO.isFriend(currentUserId, target.getId())) {
            throw new IllegalArgumentException("Hai người chưa là bạn bè.");
        }

        friendDAO.deleteFriendship(currentUserId, target.getId());
    }

    public void cancelRequest(long senderId, String targetUsername) throws IllegalArgumentException {
        if (targetUsername == null || targetUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng cung cấp tên người dùng.");
        }
        User target = userDAO.findByUsername(targetUsername.trim());
        if (target == null) throw new IllegalArgumentException("Người dùng không tồn tại.");

        friendDAO.cancelFriendRequest(senderId, target.getId());
    }
}
