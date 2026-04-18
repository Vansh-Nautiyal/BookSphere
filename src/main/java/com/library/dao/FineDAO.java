package com.library.dao;

import com.library.db.DatabaseManager;
import com.library.model.Fine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FineDAO {

    public static boolean createFine(int borrowingId, int userId, double amount, String dueDate) {
        String sql = "INSERT INTO fines (borrowing_id,user_id,amount,paid,due_date) VALUES (?,?,?,0,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, borrowingId); ps.setInt(2, userId);
            ps.setDouble(3, amount);   ps.setString(4, dueDate);
            ps.executeUpdate(); return true;
        } catch (SQLException e) {
            System.err.println("[FineDAO] Create: " + e.getMessage()); return false;
        }
    }

    public static boolean markAsPaid(int fineId) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "UPDATE fines SET paid=1,paid_date=date('now') WHERE id=?")) {
            ps.setInt(1, fineId); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static List<Fine> getAllFines() { return query(
        "SELECT f.*,u.name AS user_name,bk.title AS book_title " +
        "FROM fines f JOIN users u ON f.user_id=u.id " +
        "JOIN borrowings br ON f.borrowing_id=br.id JOIN books bk ON br.book_id=bk.id " +
        "ORDER BY f.paid ASC, f.amount DESC"); }

    public static List<Fine> getPendingFines() { return query(
        "SELECT f.*,u.name AS user_name,bk.title AS book_title " +
        "FROM fines f JOIN users u ON f.user_id=u.id " +
        "JOIN borrowings br ON f.borrowing_id=br.id JOIN books bk ON br.book_id=bk.id " +
        "WHERE f.paid=0 ORDER BY f.amount DESC"); }

    public static double getTotalPendingAmount() {
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT COALESCE(SUM(amount),0) FROM fines WHERE paid=0")) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    private static List<Fine> query(String sql) {
        List<Fine> list = new ArrayList<>();
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                Fine f = new Fine();
                f.setId(rs.getInt("id"));
                f.setBorrowingId(rs.getInt("borrowing_id"));
                f.setUserId(rs.getInt("user_id"));
                f.setUserName(rs.getString("user_name"));
                f.setBookTitle(rs.getString("book_title"));
                f.setAmount(rs.getDouble("amount"));
                f.setPaid(rs.getInt("paid") == 1);
                f.setDueDate(rs.getString("due_date"));
                f.setPaidDate(rs.getString("paid_date"));
                list.add(f);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}