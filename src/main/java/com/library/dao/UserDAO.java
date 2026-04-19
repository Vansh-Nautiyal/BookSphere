package com.library.dao;

import com.library.db.DatabaseManager;
import com.library.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private static final String[] PLACEHOLDER_VALUES = {
        "full name",
        "email address",
        "phone number",
        "e.g. stu2024001",
        "choose a password"
    };

    public static User login(String globalId, String password) {
        String sql = "SELECT * FROM users WHERE global_id=? AND password=? AND status='active'";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, globalId);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static boolean register(User user) {
        if (!isValidMemberRecord(user)) return false;
        String sql = "INSERT INTO users (global_id,name,email,phone,role,password,status) VALUES (?,?,?,?,?,?,'active')";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getGlobalId());
            ps.setString(2, user.getName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getRole());
            ps.setString(6, user.getPassword());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[UserDAO] Register: " + e.getMessage());
            return false;
        }
    }

    public static List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM users ORDER BY name")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static List<User> getMembers() {
        List<User> list = new ArrayList<>();
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery(
                 "SELECT * FROM users WHERE role IN ('student','operator') ORDER BY name")) {
            while (rs.next()) {
                User user = map(rs);
                if (isValidMemberRecord(user) && "active".equalsIgnoreCase(user.getStatus())) {
                    list.add(user);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static User getUserById(int id) {
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("SELECT * FROM users WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static boolean globalIdExists(String globalId) {
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("SELECT COUNT(*) FROM users WHERE global_id=?")) {
            ps.setString(1, globalId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public static boolean updateStatus(int id, String status) {
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("UPDATE users SET status=? WHERE id=?")) {
            ps.setString(1, status); ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public static boolean updateUser(User user) {
        if (!isValidMemberRecord(user)) return false;
        String sql = "UPDATE users SET global_id=?, name=?, email=?, phone=?, role=?, password=?, status=? " +
                     "WHERE id=? AND role!='admin'";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getGlobalId());
            ps.setString(2, user.getName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getRole());
            ps.setString(6, user.getPassword());
            ps.setString(7, user.getStatus());
            ps.setInt(8, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] Update: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteUser(int id) {
        Connection connection = null;
        boolean originalAutoCommit = true;
        try {
            connection = DatabaseManager.getConnection();
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            String role = null;
            try (PreparedStatement getRole = connection.prepareStatement(
                    "SELECT role FROM users WHERE id=?")) {
                getRole.setInt(1, id);
                try (ResultSet rs = getRole.executeQuery()) {
                    if (rs.next()) role = rs.getString("role");
                }
            }

            if (role == null || "admin".equalsIgnoreCase(role)) {
                connection.rollback();
                return false;
            }

            try (PreparedStatement activeBorrowings = connection.prepareStatement(
                    "SELECT book_id, COUNT(*) AS active_count " +
                    "FROM borrowings WHERE user_id=? AND status='active' GROUP BY book_id");
                 PreparedStatement restoreStock = connection.prepareStatement(
                    "UPDATE books SET available_copies=available_copies+? WHERE id=?")) {
                activeBorrowings.setInt(1, id);
                try (ResultSet rs = activeBorrowings.executeQuery()) {
                    while (rs.next()) {
                        restoreStock.setInt(1, rs.getInt("active_count"));
                        restoreStock.setInt(2, rs.getInt("book_id"));
                        restoreStock.executeUpdate();
                    }
                }
            }

            try (PreparedStatement deleteFinesByBorrowing = connection.prepareStatement(
                    "DELETE FROM fines WHERE borrowing_id IN (SELECT id FROM borrowings WHERE user_id=?)")) {
                deleteFinesByBorrowing.setInt(1, id);
                deleteFinesByBorrowing.executeUpdate();
            }

            try (PreparedStatement deleteUserFines = connection.prepareStatement(
                    "DELETE FROM fines WHERE user_id=?")) {
                deleteUserFines.setInt(1, id);
                deleteUserFines.executeUpdate();
            }

            try (PreparedStatement deleteBorrowings = connection.prepareStatement(
                    "DELETE FROM borrowings WHERE user_id=?")) {
                deleteBorrowings.setInt(1, id);
                deleteBorrowings.executeUpdate();
            }

            int deletedUsers;
            try (PreparedStatement deleteUser = connection.prepareStatement(
                    "DELETE FROM users WHERE id=? AND role!='admin'")) {
                deleteUser.setInt(1, id);
                deletedUsers = deleteUser.executeUpdate();
            }

            connection.commit();
            return deletedUsers > 0;
        } catch (SQLException e) {
            if (connection != null) {
                try { connection.rollback(); } catch (SQLException ignored) {}
            }
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try { connection.setAutoCommit(originalAutoCommit); } catch (SQLException ignored) {}
            }
        }
        return false;
    }

    public static int getTotalCount() {
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM users WHERE role!='admin'")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public static boolean isValidMemberRecord(User user) {
        if (user == null) return false;

        String globalId = normalize(user.getGlobalId());
        String name = normalize(user.getName());
        String role = normalize(user.getRole());
        String status = normalize(user.getStatus());

        if (globalId.isEmpty() || name.isEmpty()) return false;
        if (!role.equals("student") && !role.equals("operator")) return false;
        if (!status.isEmpty() && !status.equals("active")) return false;
        if (looksLikePlaceholder(globalId) || looksLikePlaceholder(name)) return false;
        return !globalId.equals(name);
    }

    public static boolean looksLikePlaceholder(String value) {
        String normalized = normalize(value);
        if (normalized.isEmpty()) return true;
        for (String placeholder : PLACEHOLDER_VALUES) {
            if (normalized.equals(placeholder)) return true;
        }
        return normalized.startsWith("e.g.")
            || normalized.contains("full name")
            || normalized.contains("example")
            || normalized.contains("placeholder");
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private static User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setGlobalId(rs.getString("global_id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setRole(rs.getString("role"));
        u.setPassword(rs.getString("password"));
        u.setJoinedDate(rs.getString("joined_date"));
        u.setStatus(rs.getString("status"));
        return u;
    }
}
