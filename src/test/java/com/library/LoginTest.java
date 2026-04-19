package com.library;

import com.library.dao.UserDAO;
import com.library.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginTest extends BaseTest {

    @Test
    void testValidLogin() {
        User user = UserDAO.login("ADMIN001", "admin123");

        assertNotNull(user);
        assertEquals("admin", user.getRole());
    }

    @Test
    void testInvalidLogin() {
        User user = UserDAO.login("wrong", "wrong");

        assertNull(user);
    }
}