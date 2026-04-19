package com.library;

import com.library.dao.*;
import com.library.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest extends BaseTest {

    @Test
    void testRegisterUser() {
        User u = new User("STU101", "TestUser", "user1@test.com", "999", "student", "pass");

        boolean result = UserDAO.register(u);

        assertTrue(result);
    }

    @Test
    void testDuplicateGlobalId() {
        User u1 = new User("STU101", "User1", "user1@test.com", "999", "student", "pass");
        User u2 = new User("STU101", "User2", "user2@test.com", "888", "student", "pass");

        UserDAO.register(u1);
        boolean result = UserDAO.register(u2);

        assertFalse(result);
    }

    @Test
    void testInvalidUserRejected() {
        User u = new User("", "", "", "", "student", "pass");

        boolean result = UserDAO.register(u);

        assertFalse(result);
    }
}