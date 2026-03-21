package com.ptit.socialchat.dao;

import com.ptit.socialchat.model.User;
import com.ptit.socialchat.util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setFullName(rs.getString("full_name"));
        u.setAvatar(rs.getString("avatar"));
        u.setRole(rs.getString("role"));
        return u;
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection con = DbConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findById(long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
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

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id ASC";
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

    public List<User> searchByKeyword(String keyword) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE LOWER(username) LIKE ? OR LOWER(full_name) LIKE ?";
        String pattern = "%" + keyword.toLowerCase() + "%";
        try (Connection con = DbConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void save(User user) {
        String sql = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)";
        try (Connection con = DbConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole() != null ? user.getRole() : "ROLE_USER");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(User user) {
        String sql = "UPDATE users SET full_name=?, avatar=?, password=? WHERE id=?";
        try (Connection con = DbConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getAvatar());
            ps.setString(3, user.getPassword());
            ps.setLong(4, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(long id) {
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection con = DbConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
