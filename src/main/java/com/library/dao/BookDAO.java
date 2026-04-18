package com.library.dao;

import com.library.db.DatabaseManager;
import com.library.model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    public static boolean addBook(Book b) {
        String sql = "INSERT INTO books (title,author,isbn,genre,publisher,year,total_copies,available_copies) " +
                     "VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, b.getTitle());   ps.setString(2, b.getAuthor());
            ps.setString(3, b.getIsbn());    ps.setString(4, b.getGenre());
            ps.setString(5, b.getPublisher()); ps.setInt(6, b.getYear());
            ps.setInt(7, b.getTotalCopies()); ps.setInt(8, b.getTotalCopies());
            ps.executeUpdate(); return true;
        } catch (SQLException e) {
            System.err.println("[BookDAO] Add: " + e.getMessage()); return false;
        }
    }

    public static List<Book> getAllBooks() {
        List<Book> list = new ArrayList<>();
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM books ORDER BY title")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static List<Book> searchBooks(String query) {
        List<Book> list = new ArrayList<>();
        String q = "%" + query.toLowerCase() + "%";
        String sql = "SELECT * FROM books WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ? " +
                     "OR LOWER(isbn) LIKE ? OR LOWER(genre) LIKE ? ORDER BY title";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1,q); ps.setString(2,q); ps.setString(3,q); ps.setString(4,q);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static List<Book> getAvailableBooks() {
        List<Book> list = new ArrayList<>();
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM books WHERE available_copies>0 ORDER BY title")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static Book getBookById(int id) {
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("SELECT * FROM books WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static boolean updateBook(Book b) {
        String sql = "UPDATE books SET title=?,author=?,isbn=?,genre=?,publisher=?,year=?,total_copies=? WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1,b.getTitle()); ps.setString(2,b.getAuthor());
            ps.setString(3,b.getIsbn()); ps.setString(4,b.getGenre());
            ps.setString(5,b.getPublisher()); ps.setInt(6,b.getYear());
            ps.setInt(7,b.getTotalCopies()); ps.setInt(8,b.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean deleteBook(int id) {
        try (Connection connection = DatabaseManager.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement deleteFines = connection.prepareStatement(
                        "DELETE FROM fines WHERE borrowing_id IN (SELECT id FROM borrowings WHERE book_id=?)")) {
                    deleteFines.setInt(1, id);
                    deleteFines.executeUpdate();
                }

                try (PreparedStatement deleteBorrowings = connection.prepareStatement(
                        "DELETE FROM borrowings WHERE book_id=?")) {
                    deleteBorrowings.setInt(1, id);
                    deleteBorrowings.executeUpdate();
                }

                int deleted;
                try (PreparedStatement deleteBook = connection.prepareStatement(
                        "DELETE FROM books WHERE id=?")) {
                    deleteBook.setInt(1, id);
                    deleted = deleteBook.executeUpdate();
                }

                connection.commit();
                connection.setAutoCommit(originalAutoCommit);
                return deleted > 0;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("[BookDAO] Delete: " + e.getMessage()); return false;
        }
    }

    public static boolean decrementAvailable(int bookId) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "UPDATE books SET available_copies=available_copies-1 WHERE id=? AND available_copies>0")) {
            ps.setInt(1, bookId); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean incrementAvailable(int bookId) {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                "UPDATE books SET available_copies=available_copies+1 WHERE id=?")) {
            ps.setInt(1, bookId); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static int getTotalCount() {
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM books")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public static int getIssuedCount() {
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM borrowings WHERE status='active'")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public static int getOverdueCount() {
        try (Statement s = DatabaseManager.getConnection().createStatement();
             ResultSet rs = s.executeQuery(
                 "SELECT COUNT(*) FROM borrowings WHERE status='active' AND due_date < date('now')")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private static Book map(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setId(rs.getInt("id"));
        b.setTitle(rs.getString("title"));
        b.setAuthor(rs.getString("author"));
        b.setIsbn(rs.getString("isbn"));
        b.setGenre(rs.getString("genre"));
        b.setPublisher(rs.getString("publisher"));
        b.setYear(rs.getInt("year"));
        b.setTotalCopies(rs.getInt("total_copies"));
        b.setAvailableCopies(rs.getInt("available_copies"));
        return b;
    }
}
