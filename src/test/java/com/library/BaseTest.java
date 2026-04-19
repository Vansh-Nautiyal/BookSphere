package com.library;

import com.library.db.DatabaseManager;
import org.junit.jupiter.api.BeforeEach;

public class BaseTest {

    @BeforeEach
    void setupDatabase() {
        DatabaseManager.initialize();
        DatabaseManager.resetDatabase();
    }
}