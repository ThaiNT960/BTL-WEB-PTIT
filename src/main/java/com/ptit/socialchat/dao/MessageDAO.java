package com.ptit.socialchat.dao;

import com.ptit.socialchat.model.Message;
import com.ptit.socialchat.model.User;
import com.ptit.socialchat.util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    public List<Message> getChatHistory(long user1Id, long user2Id) {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT m.id, m.content, m.timestamp, " +
                "s.id, s.username, s.full_name, " +
                "r.id, r.username, r.full_name " +
                "FROM messages m " +
                "JOIN users s ON m.sender_id = s.id " +
                "JOIN users r ON m.receiver_id = r.id " +
                "WHERE (m.sender_id=? AND m.receiver_id=?) OR (m.sender_id=? AND m.receiver_id=?) " +
                "ORDER BY m.timestamp ASC";
        try (Connection con = DbConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, user1Id);
            ps.setLong(2, user2Id);
            ps.setLong(3, user2Id);
            ps.setLong(4, user1Id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Message msg = new Message();
                msg.setId(rs.getLong(1));
                msg.setContent(rs.getString(2));
                Timestamp ts = rs.getTimestamp(3);
                msg.setTimestamp(ts != null ? ts.toLocalDateTime().toString() : "");
                User sender = new User();
                sender.setId(rs.getLong(4));
                sender.setUsername(rs.getString(5));
                sender.setFullName(rs.getString(6));
                msg.setSender(sender);
                User receiver = new User();
                receiver.setId(rs.getLong(7));
                receiver.setUsername(rs.getString(8));
                receiver.setFullName(rs.getString(9));
                msg.setReceiver(receiver);
                list.add(msg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void save(long senderId, long receiverId, String content) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, content, timestamp) VALUES (?, ?, ?, NOW())";
        try (Connection con = DbConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, senderId);
            ps.setLong(2, receiverId);
            ps.setString(3, content);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
