package com.ptit.socialchat.dao;

import com.ptit.socialchat.model.FriendRequest;
import com.ptit.socialchat.model.User;
import com.ptit.socialchat.util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendDAO {

    public List<User> getFriendsByUserId(long userId) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.full_name, u.avatar " +
                "FROM friends f JOIN users u ON f.friend_id = u.id " +
                "WHERE f.user_id = ? ORDER BY u.full_name ASC";
        try (Connection con = DbConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getLong(1));
                u.setUsername(rs.getString(2));
                u.setFullName(rs.getString(3));
                u.setAvatar(rs.getString(4));
                list.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void addFriendship(long userId, long friendId) {
        String sql = "INSERT INTO friends (user_id, friend_id, created_at) VALUES (?, ?, NOW())";
        try (Connection con = DbConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, friendId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<FriendRequest> getPendingRequests(long receiverId) {
        List<FriendRequest> list = new ArrayList<>();
        String sql = "SELECT fr.id, fr.status, fr.created_at, " +
                "u.id, u.username, u.full_name " +
                "FROM friend_requests fr JOIN users u ON fr.sender_id = u.id " +
                "WHERE fr.receiver_id = ? AND fr.status = 'PENDING' ORDER BY fr.created_at DESC";
        try (Connection con = DbConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, receiverId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                FriendRequest req = new FriendRequest();
                req.setId(rs.getLong(1));
                req.setStatus(rs.getString(2));
                Timestamp ts = rs.getTimestamp(3);
                req.setCreatedAt(ts != null ? ts.toLocalDateTime().toString() : "");
                User sender = new User();
                sender.setId(rs.getLong(4));
                sender.setUsername(rs.getString(5));
                sender.setFullName(rs.getString(6));
                req.setSender(sender);
                list.add(req);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public FriendRequest findRequestById(long requestId) {
        String sql = "SELECT fr.id, fr.status, fr.sender_id, fr.receiver_id " +
                "FROM friend_requests fr WHERE fr.id = ?";
        try (Connection con = DbConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, requestId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                FriendRequest req = new FriendRequest();
                req.setId(rs.getLong(1));
                req.setStatus(rs.getString(2));
                User sender = new User();
                sender.setId(rs.getLong(3));
                req.setSender(sender);
                User receiver = new User();
                receiver.setId(rs.getLong(4));
                req.setReceiver(receiver);
                return req;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendRequest(long senderId, long receiverId) {
        String sql = "INSERT INTO friend_requests (sender_id, receiver_id, status, created_at) VALUES (?, ?, 'PENDING', NOW())";
        try (Connection con = DbConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, senderId);
            ps.setLong(2, receiverId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateRequestStatus(long requestId, String status) {
        String sql = "UPDATE friend_requests SET status=? WHERE id=?";
        try (Connection con = DbConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, requestId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
