package com.library;

import com.library.db.DatabaseManager;
import com.library.ui.LoginScreen;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // 1. Initialize database and create tables
        DatabaseManager.initialize();

        // 2. Apply cross-platform L&F with dark dialog overrides
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("OptionPane.background",        new java.awt.Color(28, 28, 28));
            UIManager.put("Panel.background",             new java.awt.Color(28, 28, 28));
            UIManager.put("OptionPane.messageForeground", new java.awt.Color(245, 245, 240));
            UIManager.put("Button.background",            new java.awt.Color(50, 50, 50));
            UIManager.put("Button.foreground",            new java.awt.Color(245, 245, 240));
        } catch (Exception ignored) {}

        // 3. Launch login screen on the EDT
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }
}