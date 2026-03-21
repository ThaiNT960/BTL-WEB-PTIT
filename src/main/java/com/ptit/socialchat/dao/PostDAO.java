package com.ptit.socialchat.dao;

import com.ptit.socialchat.model.Comment;
import com.ptit.socialchat.model.Post;
import com.ptit.socialchat.model.User;
import com.ptit.socialchat.util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostDAO {

    private Post mapRow(ResultSet rs) throws SQLException {
        Post p = new Post();
        p.setId(rs.getLong("p.id"));
        p.setContent(rs.getString("p.content"));
        p.setImageUrl(rs.getString("p.image_url"));
        Timestamp ts = rs.getTimestamp("p.created_at");
        p.setCreatedAt(ts != null ? ts.toLocalDateTime().toString() : "");

        User u = new User();
        u.setId(rs.getLong("u.id"));
        u.setUsername(rs.getString("u.username"));
        u.setFullName(rs.getString("u.full_name"));
        u.setAvatar(rs.getString("u.avatar"));
        p.setUser(u);
        return p;
    }

    public List<Post> findAllOrderByCreatedAtDesc() {
        List<Post> list = new ArrayList<>();
        String sql = "SELECT p.id, p.content, p.image_url, p.created_at, " +
                "u.id, u.username, u.full_name, u.avatar " +
                "FROM posts p JOIN users u ON p.user_id = u.id " +
                "ORDER BY p.created_at DESC";
        try (Connection con = DbConnection.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Post findById(long id) {
        String sql = "SELECT p.id, p.content, p.image_url, p.created_at, " +
                "u.id, u.username, u.full_name, u.avatar " +
                "FROM posts p JOIN users u ON p.user_id = u.id WHERE p.id = ?";
        try (Connection con = DbConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public long save(String content, String imageUrl, long userId) {
        String sql = "INSERT INTO posts (content, image_url, user_id, created_at) VALUES (?, ?, ?, NOW())";
        try (Connection con = DbConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, content);
            ps.setString(2, imageUrl);
            ps.setLong(3, userId);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next())
                return rs.getLong(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void delete(long id) {
        String sql = "DELETE FROM posts WHERE id=?";
        try (Connection con = DbConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Comment> findCommentsByPostId(long postId) {
        List<Comment> list = new ArrayList<>();
        String sql = "SELECT c.id, c.content, c.created_at, u.id, u.username, u.full_name " +
                "FROM comments c JOIN users u ON c.user_id = u.id " +
                "WHERE c.post_id = ? ORDER BY c.created_at ASC";
        try (Connection con = DbConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, postId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Comment c = new Comment();
                c.setId(rs.getLong(1));
                c.setContent(rs.getString(2));
                Timestamp ts = rs.getTimestamp(3);
                c.setCreatedAt(ts != null ? ts.toLocalDateTime().toString() : "");
                User u = new User();
                u.setId(rs.getLong(4));
                u.setUsername(rs.getString(5));
                u.setFullName(rs.getString(6));
                c.setUser(u);
                c.setPostId(postId);
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void addComment(long postId, String content, long userId) {
        String sql = "INSERT INTO comments (content, post_id, user_id, created_at) VALUES (?, ?, ?, NOW())";
        try (Connection con = DbConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, content);
            ps.setLong(2, postId);
            ps.setLong(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getLikeCount(long postId) {
        String sql = "SELECT COUNT(*) FROM post_likes WHERE post_id=?";
        try (Connection con = DbConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, postId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean isLikedByUser(long postId, long userId) {
        String sql = "SELECT COUNT(*) FROM post_likes WHERE post_id=? AND user_id=?";
        try (Connection con = DbConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, postId);
            ps.setLong(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean toggleLike(long postId, long userId) {
        if (isLikedByUser(postId, userId)) {
            // Unlike
            String sql = "DELETE FROM post_likes WHERE post_id=? AND user_id=?";
            try (Connection con = DbConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setLong(1, postId);
                ps.setLong(2, userId);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        } else {
            // Like
            String sql = "INSERT INTO post_likes (post_id, user_id, created_at) VALUES (?, ?, NOW())";
            try (Connection con = DbConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setLong(1, postId);
                ps.setLong(2, userId);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return true;
        }
    }
}
