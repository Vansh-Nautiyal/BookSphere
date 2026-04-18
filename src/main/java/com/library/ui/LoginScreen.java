package com.library.ui;

import com.library.dao.UserDAO;
import com.library.model.User;
import com.library.HomePage;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

import static com.library.ThemeColors.*;

public class LoginScreen extends JFrame {

    private JTextField globalIdField;
    private JPasswordField passwordField;
    private JPanel card;

    // Register fields
    private JTextField regGlobalId, regName, regEmail, regPhone;
    private JPasswordField regPassword;
    private JComboBox<String> regRole;

    public LoginScreen() {
        setTitle("BookSphere — Sign In");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(960, 620);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new GridLayout(1, 2, 0, 0));
        root.add(buildLeft());
        root.add(buildRight());
        setContentPane(root);
    }

    // ── Left decorative panel ──────────────────────────────────────────────────
    private JPanel buildLeft() {
        JPanel left = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(12, 12, 12));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 5));
                for (int i = -getHeight(); i < getWidth(); i += 32)
                    g2.drawLine(i, 0, i + getHeight(), getHeight());
                g2.setColor(RED);
                g2.fillRect(0, 0, getWidth(), 10);
                g2.setColor(GOLD);
                g2.fillRect(0, 10, getWidth(), 4);
            }
        };
        left.setLayout(new GridBagLayout());

        // JPanel content = new JPanel();
        // content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        // content.setBorder(new EmptyBorder(0, 40, 0, 40));
        // content.setBackground(new Color(28, 28, 28));
        // content.setBorder(new EmptyBorder(30, 40, 30, 40));
        // content.setOpaque(true);

        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background with rounded corners
                g2.setColor(new Color(28, 28, 28));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Optional border
                g2.setColor(new Color(70, 70, 70));
                g2.drawRoundRect(2, 2, getWidth()-2, getHeight()-4 , 20, 20);
            }
        };
        content.setOpaque(false); // must be false
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel appTitle = new JLabel("BookSphere");
        appTitle.setFont(new Font("Georgia", Font.BOLD, 32));
        appTitle.setForeground(WHITE);
        JLabel tagline = new JLabel("Digital Library Management System");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tagline.setForeground(MUTED);

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JLabel desc = new JLabel(
                "<html><body style='width:240px; color:#888;'>" +
                        "BookSphere brings every corner of your library into one seamless digital ecosystem." +
                        "<br><br>Efficient. Intelligent. Connected." +
                        "<br><br>Manage your library with clarity and confidence — all in one place." +
                        "</body></html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        content.add(Box.createVerticalStrut(18));
        content.add(appTitle);
        content.add(Box.createVerticalStrut(4));
        content.add(tagline);
        content.add(Box.createVerticalStrut(24));
        content.add(sep);
        content.add(Box.createVerticalStrut(24));
        content.add(desc);
        content.add(Box.createVerticalStrut(20));

        left.add(content);
        return left;
    }

    // ── Right panel ────────────────────────────────────────────────────────────
    private JPanel buildRight() {
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(BG_DARK);
        card = new JPanel();
        card.setBackground(BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(36, 40, 36, 40));
        card.setPreferredSize(new Dimension(380, 460));
        showLogin();
        right.add(card);
        return right;
    }

    // ── Login form ─────────────────────────────────────────────────────────────
    private void showLogin() {
        card.removeAll();

        JLabel heading = label24("Sign in");
        JLabel sub = labelMuted("Use your Global ID and password");

        globalIdField = field("Global ID  (e.g. STU001)");
        passwordField = passField("Password");

        JButton btn = bigButton("Sign in", RED, WHITE);
        btn.addActionListener(e -> doLogin());
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel switchRow = switchRow("Don't have an account?  ", "Register here", GOLD, this::showRegister);

        card.add(heading);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(28));
        card.add(fl("Global ID"));
        card.add(Box.createVerticalStrut(5));
        card.add(globalIdField);
        card.add(Box.createVerticalStrut(16));
        card.add(fl("Password"));
        card.add(Box.createVerticalStrut(5));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(24));
        card.add(btn);
        card.add(Box.createVerticalStrut(16));
        card.add(switchRow);
        card.add(Box.createVerticalGlue());
        card.revalidate();
        card.repaint();
    }

    // ── Register form ──────────────────────────────────────────────────────────
    private void showRegister() {
        card.removeAll();

        JLabel heading = label24("Create account");

        regGlobalId = field("e.g. STU2024001");
        regName = field("Full name");
        regEmail = field("Email address");
        regPhone = field("Phone number");
        regPassword = passField("Choose a password");
        regRole = new JComboBox<>(new String[] { "student", "member" });
        styleCombo(regRole);

        JButton btn = bigButton("Create account", GOLD, BG_BLACK);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> doRegister());

        JPanel switchRow = switchRow("Already have an account?  ", "Sign in", RED, this::showLogin);

        JPanel twoCol = new JPanel(new GridLayout(1, 2, 10, 0));
        twoCol.setOpaque(false);
        twoCol.setAlignmentX(Component.LEFT_ALIGNMENT);
        twoCol.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        twoCol.add(miniCol("Role", regRole));
        twoCol.add(miniCol("Phone", regPhone));

        card.add(heading);
        card.add(Box.createVerticalStrut(20));
        card.add(fl("Global ID"));
        card.add(Box.createVerticalStrut(4));
        card.add(regGlobalId);
        card.add(Box.createVerticalStrut(10));
        card.add(fl("Full Name"));
        card.add(Box.createVerticalStrut(4));
        card.add(regName);
        card.add(Box.createVerticalStrut(10));
        card.add(fl("Email"));
        card.add(Box.createVerticalStrut(4));
        card.add(regEmail);
        card.add(Box.createVerticalStrut(10));
        card.add(twoCol);
        card.add(Box.createVerticalStrut(10));
        card.add(fl("Password"));
        card.add(Box.createVerticalStrut(4));
        card.add(regPassword);
        card.add(Box.createVerticalStrut(20));
        card.add(btn);
        card.add(Box.createVerticalStrut(14));
        card.add(switchRow);
        card.revalidate();
        card.repaint();
    }

    // ── Actions ────────────────────────────────────────────────────────────────
    private void doLogin() {
        String id = globalIdField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();
        if (id.isEmpty() || pass.isEmpty()) {
            error("Enter your Global ID and password.");
            return;
        }
        User user = UserDAO.login(id, pass);
        if (user == null) {
            error("Invalid credentials or account suspended.");
            return;
        }
        dispose();
        SwingUtilities.invokeLater(() -> new HomePage(user).setVisible(true));
    }

    private void doRegister() {
        String gid = regGlobalId.getText().trim();
        String name = regName.getText().trim();
        String email = regEmail.getText().trim();
        String phone = regPhone.getText().trim();
        String pass = new String(regPassword.getPassword()).trim();
        String role = (String) regRole.getSelectedItem();

        if (gid.isEmpty() || name.isEmpty() || pass.isEmpty()) {
            error("Global ID, name, and password are required.");
            return;
        }
        if (UserDAO.looksLikePlaceholder(gid) || UserDAO.looksLikePlaceholder(name)) {
            error("Please replace the example text with the member's actual ID and name.");
            return;
        }
        if (UserDAO.globalIdExists(gid)) {
            error("Global ID already registered.");
            return;
        }

        User u = new User(gid, name, email, phone, role, pass);
        if (UserDAO.register(u)) {
            JOptionPane.showMessageDialog(this, "Account created! You can now sign in.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            showLogin();
        } else {
            error("Registration failed. Please try again.");
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private JLabel label24(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Georgia", Font.BOLD, 24));
        l.setForeground(WHITE);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel labelMuted(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel fl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField field(String ph) {
        JTextField f = new JTextField(ph);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setForeground(MUTED);
        f.setBackground(BG_DARK);
        f.setCaretColor(WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true), new EmptyBorder(8, 12, 8, 12)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(ph)) {
                    f.setText("");
                    f.setForeground(WHITE);
                }
            }

            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) {
                    f.setText(ph);
                    f.setForeground(MUTED);
                }
            }
        });
        return f;
    }

    private JPasswordField passField(String ph) {
        JPasswordField f = new JPasswordField(ph);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setForeground(MUTED);
        f.setBackground(BG_DARK);
        f.setCaretColor(WHITE);
        f.setEchoChar((char) 0);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true), new EmptyBorder(8, 12, 8, 12)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (new String(f.getPassword()).equals(ph)) {
                    f.setText("");
                    f.setEchoChar('●');
                    f.setForeground(WHITE);
                }
            }

            public void focusLost(FocusEvent e) {
                if (new String(f.getPassword()).isEmpty()) {
                    f.setEchoChar((char) 0);
                    f.setText(ph);
                    f.setForeground(MUTED);
                }
            }
        });
        return f;
    }

    private void styleCombo(JComboBox<String> c) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.setBackground(BG_DARK);
        c.setForeground(WHITE);
        c.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JButton bigButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        return btn;
    }

    private JPanel switchRow(String msg, String linkText, Color linkColor, Runnable action) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(msg);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(MUTED);
        JLabel link = new JLabel(linkText);
        link.setFont(new Font("Segoe UI", Font.BOLD, 12));
        link.setForeground(linkColor);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                action.run();
            }
        });
        row.add(lbl);
        row.add(link);
        return row;
    }

    private JPanel miniCol(String label, JComponent field) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(fl(label));
        p.add(Box.createVerticalStrut(4));
        p.add(field);
        return p;
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
