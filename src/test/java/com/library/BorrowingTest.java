package com.library;

import com.library.dao.*;
import com.library.model.Book;
import com.library.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BorrowingTest extends BaseTest {

    int userId;
    int bookId;
    int borrowingId;

    @BeforeEach
    void prepareData() {

        // Create valid user
        User user = new User(
                "STU200",
                "BorrowUser",
                "borrow@test.com",
                "9999999999",
                "student",
                "pass"
        );

        UserDAO.register(user);

        User savedUser = UserDAO.login("STU200", "pass");
        assertNotNull(savedUser);
        userId = savedUser.getId();

        // Create book
        Book book = new Book("Test Book", "Author", "111", "Tech", "Pub", 2024, 1);
        BookDAO.addBook(book);
        bookId = BookDAO.getAllBooks().get(0).getId();

        // Issue book
        boolean issued = BorrowingDAO.issueBook(bookId, userId);
        assertTrue(issued);

        List<?> list = BorrowingDAO.getActiveBorrowings();
        assertFalse(list.isEmpty());

        borrowingId = BorrowingDAO.getActiveBorrowings().get(0).getId();
    }

    @Test
    void testIssueBook() {
        assertTrue(borrowingId > 0);
    }

    @Test
    void testReturnBook() {
        boolean result = BorrowingDAO.returnBook(borrowingId);
        assertTrue(result);
    }

    @Test
    void testFineCalculation() {
        double fine = BorrowingDAO.calculateFine("2020-01-01");
        assertTrue(fine > 0);
    }
}