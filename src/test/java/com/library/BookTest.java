package com.library;

import com.library.dao.*;
import com.library.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookTest extends BaseTest {

    int bookId;

    @BeforeEach
    void prepareBook() {
        Book b = new Book("Java", "Author", "123", "Tech", "Pub", 2024, 3);
        BookDAO.addBook(b);

        bookId = BookDAO.getAllBooks().get(0).getId();
    }

    @Test
    void testAddBook() {
        assertTrue(bookId > 0);
    }

    @Test
    void testSearchBook() {
        assertFalse(BookDAO.searchBooks("Java").isEmpty());
    }

    @Test
    void testDeleteBook() {
        boolean result = BookDAO.deleteBook(bookId);
        assertTrue(result);
    }
}