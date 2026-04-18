package com.library.dao;

import com.library.db.DatabaseManager;
import com.library.model.Borrowing;
import com.library.model.User;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowingDAO {

    public static final int LOAN_DAYS    = 28;
    public static final int FINE_PER_DAY = 10;

    public static boolean issueBook(int bookId, int userId) {
        User user = UserDAO.getUserById(userId);
        if (!UserDAO.isValidMemberRecord(user)) return false;
        if (hasActiveBorrowing(bookId, userId)) return false;
        if (!BookDAO.decrementAvailable(bookId)) return false;
        String due = LocalDate.now().plusDays(LOAN_DAYS).toString();
        String sql = "INSERT INTO borrowings (book_id,user_id,borrow_date,due_date,status) " +
                     "VALUES (?,?,date('now'),?,'active')";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, bookId); ps.setInt(2, userId); ps.setString(3, due);
            ps.executeUpdate(); return true;
        } catch (SQLException e) {
            System.err.println("[BorrowingDAO] Issue: " + e.getMessage());
            BookDAO.incrementAvailable(bookId); return false;
        }
    }

    public static boolean hasActiveBorrowing(int bookId, int userId) {
        String sql = "SELECT COUNT(*) FROM borrowings WHERE book_id=? AND user_id=? AND status='active'";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean returnBook(int borrowingId) {
        Borrowing b = getById(borrowingId);
        if (b == null || b.isReturned()) return false;
        double fine = calculateFine(b.getDueDate());
        String sql = "UPDATE borrowings SET return_date=date('now'),fine=?,status='returned' WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setDouble(1, fine); ps.setInt(2, borrowingId);
            ps.executeUpdate();
            BookDAO.incrementAvailable(b.getBookId());
            if (fine > 0) FineDAO.createFine(borrowingId, b.getUserId(), fine, b.getDueDate());
            return true;
        } catch (SQLException e) {
            System.err.println("[BorrowingDAO] Return: " + e.getMessage()); return false;
        }
    }

    public static double calculateFine(String dueDateStr) {
        try {
            LocalDate due   = LocalDate.parse(dueDateStr);
            LocalDate today = LocalDate.now();
            if (today.isAfter(due)) return (today.toEpochDay() - due.toEpochDay()) * FINE_PER_DAY;
        } catch (Exception e) { e.printStackTrace(); }
        return 0.0;
    }

    public static List<Borrowing> getActiveBorrowings() { return query(
        "SELECT b.*,bk.title AS book_title,u.name AS user_name " +
        "FROM borrowings b JOIN books bk ON b.book_id=bk.id JOIN users u ON b.user_id=u.id " +
        "WHERE b.status='active' ORDER BY b.due_date ASC"); }

    public static List<Borrowing> getAllBorrowings() { return query(
        "SELECT b.*,bk.title AS book_title,u.name AS user_name " +
        "FROM borrowings b JOIN books bk ON b.book_id=bk.id JOIN users u ON b.user_id=u.id " +
        "ORDER BY b.borrow_date DESC"); }

    public static List<Borrowing> getOverdueBorrowings() {
        List<Borrowing> list = query(
            "SELECT b.*,bk.title AS book_title,u.name AS user_name " +
            "FROM borrowings b JOIN books bk ON b.book_id=bk.id JOIN users u ON b.user_id=u.id " +
            "WHERE b.status='active' AND b.due_date < date('now') ORDER BY b.due_date ASC");
        list.forEach(b -> b.setFine(calculateFine(b.getDueDate())));
        return list;
    }

    public static Borrowing getById(int id) {
        List<Borrowing> list = query(
            "SELECT b.*,bk.title AS book_title,u.name AS user_name " +
            "FROM borrowings b JOIN books bk ON b.book_id=bk.id JOIN users u ON b.user_id=u.id " +
            "WHERE b.id=" + id);
        return list.isEmpty() ? null : list.get(0);
    }

    private static List<Borrowing> query(String sql) {
        List<Borrowing> list = new ArrayList<>();
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                Borrowing b = new Borrowing();
                b.setId(rs.getInt("id"));
                b.setBookId(rs.getInt("book_id"));
                b.setUserId(rs.getInt("user_id"));
                b.setBookTitle(rs.getString("book_title"));
                b.setUserName(rs.getString("user_name"));
                b.setBorrowDate(rs.getString("borrow_date"));
                b.setDueDate(rs.getString("due_date"));
                b.setReturnDate(rs.getString("return_date"));
                b.setFine(rs.getDouble("fine"));
                b.setStatus(rs.getString("status"));
                list.add(b);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
