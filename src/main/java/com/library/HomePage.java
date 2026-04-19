package com.library;

import com.library.dao.BookDAO;
import com.library.dao.FineDAO;
import com.library.dao.UserDAO;
import com.library.db.DatabaseManager;
import com.library.model.User;
import com.library.ui.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

import static com.library.ThemeColors.*;

public class HomePage extends JFrame {

    private final User currentUser;
    private JPanel contentArea;
    private JPanel sidebarNav;
    private String activeSection = "Dashboard";
    private JLabel topBarTitle;

    // Cached panels (lazy-loaded)
    private BookPanel bookPanel;
    private OperatorPanel memberPanel;
    private BorrowPanel borrowPanel;
    private FinePanel finePanel;
    private ReportPanel reportPanel;

    public HomePage(User user) {
        this.currentUser = user;
        setTitle("BookSphere — " + user.getName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 680));
        setSize(new Dimension(1200, 720));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_BLACK);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_BLACK);
        setContentPane(root);

        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildRightPane(), BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SIDEBAR
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBackground(BG_DARK);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));

        // Brand
        JPanel brand = new JPanel();
        brand.setBackground(BG_DARK);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(new EmptyBorder(28, 24, 20, 24));

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        logoRow.setBackground(BG_DARK);
        JPanel logoIcon = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(RED);
                g2.fillRoundRect(0, 0, 32, 32, 8, 8);
                g2.setColor(WHITE);
                g2.setFont(new Font("Georgia", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("B", (32 - fm.stringWidth("L")) / 2, 22);
            }
        };
        logoIcon.setPreferredSize(new Dimension(32, 32));
        logoIcon.setOpaque(false);
        JLabel brandName = new JLabel("  BookSphere");
        brandName.setFont(FONT_BRAND);
        brandName.setForeground(WHITE);
        logoRow.add(logoIcon);
        logoRow.add(brandName);

        JLabel brandSub = new JLabel("Management System");
        brandSub.setFont(FONT_SMALL);
        brandSub.setForeground(MUTED);
        brandSub.setBorder(new EmptyBorder(5, 0, 0, 0));
        brand.add(logoRow);
        brand.add(brandSub);

        JPanel divider = new JPanel();
        divider.setBackground(BORDER);
        divider.setPreferredSize(new Dimension(0, 1));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Nav
        sidebarNav = new JPanel();
        sidebarNav.setBackground(BG_DARK);
        sidebarNav.setLayout(new BoxLayout(sidebarNav, BoxLayout.Y_AXIS));
        sidebarNav.setBorder(new EmptyBorder(12, 0, 12, 0));
        rebuildNav();

        // User panel
        JPanel userPanel = new JPanel(new BorderLayout(10, 0));
        userPanel.setBackground(new Color(15, 15, 15));
        userPanel.setBorder(new EmptyBorder(14, 20, 14, 20));

        JPanel avatar = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(currentUser.getRole().equals("admin") ? GOLD : RED);
                g2.fillOval(0, 0, 36, 36);
                g2.setColor(BG_BLACK);
                g2.setFont(new Font("Georgia", Font.BOLD, 13));
                String initials = getInitials(currentUser.getName());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials, (36 - fm.stringWidth(initials)) / 2, 23);
            }
        };
        avatar.setPreferredSize(new Dimension(36, 36));
        avatar.setOpaque(false);

        JPanel userText = new JPanel();
        userText.setBackground(new Color(15, 15, 15));
        userText.setLayout(new BoxLayout(userText, BoxLayout.Y_AXIS));
        JLabel uName = new JLabel(currentUser.getName());
        uName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        uName.setForeground(WHITE);
        JLabel uRole = new JLabel(capitalize(currentUser.getRole()) + " · " + currentUser.getGlobalId());
        uRole.setFont(FONT_SMALL);
        uRole.setForeground(currentUser.getRole().equals("admin") ? GOLD : MUTED);
        userText.add(uName);
        userText.add(uRole);

        userPanel.add(avatar, BorderLayout.WEST);
        userPanel.add(userText, BorderLayout.CENTER);

        JButton logoutBtn = new JButton("Logout") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(60, 20, 20) : new Color(40, 15, 15));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                super.paintComponent(g);
            }
        };
        logoutBtn.setFont(FONT_SMALL);
        logoutBtn.setForeground(new Color(160, 60, 60));
        logoutBtn.setOpaque(false);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorder(new EmptyBorder(4, 10, 4, 10));
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
        });

        JPanel bottomArea = new JPanel(new BorderLayout());
        bottomArea.setBackground(new Color(15, 15, 15));
        bottomArea.add(userPanel, BorderLayout.CENTER);
        bottomArea.add(logoutBtn, BorderLayout.EAST);

        JPanel navSection = new JPanel(new BorderLayout());
        navSection.setBackground(BG_DARK);
        navSection.add(divider, BorderLayout.NORTH);
        navSection.add(sidebarNav, BorderLayout.CENTER);

        JPanel sideContent = new JPanel(new BorderLayout());
        sideContent.setBackground(BG_DARK);
        sideContent.add(brand, BorderLayout.NORTH);
        sideContent.add(navSection, BorderLayout.CENTER);

        sidebar.add(sideContent, BorderLayout.CENTER);
        sidebar.add(bottomArea, BorderLayout.SOUTH);
        return sidebar;
    }

    private void rebuildNav() {
        sidebarNav.removeAll();
        String[][] navItems = {
                { "D", "Dashboard" },
                { "B", "Books" },
                { "M", "Members" },
                { "F", "Fines" },
                { "R", "Reports" },
        };
        if (!currentUser.getRole().equals("operator")) {
            buildNavItem("I", "Borrow / Return");
        }

        for (String[] item : navItems) {

            // Hide Reports from non-admins
            if (item[1].equals("Reports") && !currentUser.getRole().equals("admin"))
                continue;

            // Hide Members for students
            if (item[1].equals("Members") && currentUser.getRole().equals("student"))
                continue;

            sidebarNav.add(buildNavItem(item[0], item[1]));
        }

        sidebarNav.revalidate();
        sidebarNav.repaint();
    }

    private JPanel buildNavItem(String icon, String label) {
        boolean active = label.equals(activeSection);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(active ? new Color(192, 39, 45, 25) : BG_DARK);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, active ? 3 : 0, 0, 0, active ? RED : BG_DARK),
                new EmptyBorder(0, active ? 18 : 21, 0, 16)));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        iconLbl.setForeground(active ? RED : MUTED);
        iconLbl.setPreferredSize(new Dimension(24, 44));

        JLabel textLbl = new JLabel(label);
        textLbl.setFont(active ? new Font("Segoe UI", Font.BOLD, 13) : FONT_BODY);
        textLbl.setForeground(active ? WHITE : MUTED);
        textLbl.setBorder(new EmptyBorder(0, 8, 0, 0));

        panel.add(iconLbl, BorderLayout.WEST);
        panel.add(textLbl, BorderLayout.CENTER);

        panel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!active) {
                    panel.setBackground(BG_HOVER);
                    textLbl.setForeground(WHITE);
                }
            }

            public void mouseExited(MouseEvent e) {
                if (!active) {
                    panel.setBackground(BG_DARK);
                    textLbl.setForeground(MUTED);
                }
            }

            public void mouseClicked(MouseEvent e) {
                activeSection = label;
                rebuildNav();
                topBarTitle.setText(label);
                showSection(label);
            }
        });
        return panel;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // RIGHT PANE
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildRightPane() {
        JPanel pane = new JPanel(new BorderLayout(0, 0));
        pane.setBackground(BG_BLACK);
        pane.add(buildTopBar(), BorderLayout.NORTH);

        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(BG_BLACK);

        JScrollPane scroll = new JScrollPane(contentArea);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_BLACK);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        pane.add(scroll, BorderLayout.CENTER);

        showSection("Dashboard");
        return pane;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_DARK);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(14, 28, 14, 28)));

        topBarTitle = new JLabel("Dashboard");
        topBarTitle.setFont(FONT_TITLE);
        topBarTitle.setForeground(WHITE);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(BG_DARK);

        JTextField search = new JTextField("Search books, members…");
        search.setPreferredSize(new Dimension(220, 34));
        search.setBackground(BG_CARD);
        search.setForeground(MUTED);
        search.setCaretColor(WHITE);
        search.setFont(FONT_BODY);
        search.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(0, 12, 0, 12)));
        search.addFocusListener(new FocusAdapter() {
            final String ph = "Search books, members…";

            public void focusGained(FocusEvent e) {
                if (search.getText().equals(ph)) {
                    search.setText("");
                    search.setForeground(WHITE);
                }
            }

            public void focusLost(FocusEvent e) {
                if (search.getText().isEmpty()) {
                    search.setText(ph);
                    search.setForeground(MUTED);
                }
            }
        });
        search.addActionListener(e -> {
            String q = search.getText().trim();
            if (!q.isEmpty() && !q.equals("Search books, members…")) {
                activeSection = "Books";
                rebuildNav();
                topBarTitle.setText("Books");
                if (bookPanel == null)
                    bookPanel = new BookPanel(currentUser);
                bookPanel.loadBooks(q);
                setContent(bookPanel);
            }
        });

        right.add(search);

        if (currentUser.getRole().equals("admin")) {
            JButton reportBtn = filledButton("Generate Report", RED, WHITE);
            reportBtn.addActionListener(e -> {
                activeSection = "Reports";
                rebuildNav();
                topBarTitle.setText("Reports");
                showSection("Reports");
            });
            JButton resetBtn = filledButton("Reset Database", GOLD, BG_BLACK);
            resetBtn.addActionListener(e -> resetDatabase());
            right.add(reportBtn);
            right.add(resetBtn);
        }

        bar.add(topBarTitle, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SECTION ROUTER
    // ══════════════════════════════════════════════════════════════════════════
    private void showSection(String section) {
        switch (section) {
            case "Dashboard":
                showDashboard();
                break;

            case "Books":
                if (bookPanel == null)
                    bookPanel = new BookPanel(currentUser);
                setContent(bookPanel);
                break;

            case "Members":
                // Block students completely
                if (currentUser.getRole().equals("student")) {
                    JOptionPane.showMessageDialog(this, "Access denied.");
                    return;
                }

                if (memberPanel == null)
                    memberPanel = new OperatorPanel(currentUser);
                else
                    memberPanel.loadMembers();
                setContent(memberPanel);
                break;

            case "Borrow / Return":
                if (currentUser.getRole().equals("operator")) {
                    JOptionPane.showMessageDialog(this,
                            "Operators are not allowed to issue or return books.",
                            "Access Denied",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                setContent(new BorrowPanel(currentUser));
                break;

            case "Fines":
                if (finePanel == null)
                    finePanel = new FinePanel(currentUser);
                else
                    finePanel.loadFines();
                setContent(finePanel);
                break;

            case "Reports":
                if (currentUser.getRole().equals("admin")) {
                    if (reportPanel == null)
                        reportPanel = new ReportPanel(currentUser);
                    setContent(reportPanel);
                }
                break;
        }
    }

    private void setContent(JPanel panel) {
        contentArea.removeAll();
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_BLACK);
        wrapper.setBorder(new EmptyBorder(24, 24, 24, 24));
        wrapper.add(panel, BorderLayout.CENTER);
        contentArea.add(wrapper, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DASHBOARD
    // ══════════════════════════════════════════════════════════════════════════
    private void showDashboard() {
        JPanel dash = new JPanel();
        dash.setBackground(BG_BLACK);
        dash.setLayout(new BoxLayout(dash, BoxLayout.Y_AXIS));

        // Welcome banner
        JPanel banner = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(RED_DARK);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(255, 255, 255, 5));
                for (int i = -getHeight(); i < getWidth(); i += 36)
                    g2.drawLine(i, 0, i + getHeight(), getHeight());
            }
        };
        banner.setOpaque(false);
        banner.setBorder(new EmptyBorder(22, 28, 22, 28));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JPanel bannerText = new JPanel();
        bannerText.setOpaque(false);
        bannerText.setLayout(new BoxLayout(bannerText, BoxLayout.Y_AXIS));
        JLabel wTitle = new JLabel("Welcome back, " + currentUser.getName());
        wTitle.setFont(new Font("Georgia", Font.BOLD, 20));
        wTitle.setForeground(WHITE);
        JLabel wSub = new JLabel(java.time.LocalDate.now().toString() + "  ·  " +
                capitalize(currentUser.getRole()) + " account");
        wSub.setFont(FONT_BODY);
        wSub.setForeground(new Color(220, 180, 180));
        bannerText.add(wTitle);
        bannerText.add(Box.createVerticalStrut(4));
        bannerText.add(wSub);

        JPanel bannerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bannerRight.setOpaque(false);
        if (!currentUser.getRole().equals("operator")) {
            JButton issueBtn = new JButton("+ Issue Book");
            bannerRight.add(issueBtn);
            issueBtn.addActionListener(e -> {
                activeSection = "Borrow / Return";
                rebuildNav();
                topBarTitle.setText("Borrow / Return");
                showSection("Borrow / Return");
            });
        }
        banner.add(bannerText, BorderLayout.WEST);
        banner.add(bannerRight, BorderLayout.EAST);

        // Live stats from DB
        int totalBooks = BookDAO.getTotalCount();
        int issued = BookDAO.getIssuedCount();
        int overdue = BookDAO.getOverdueCount();
        double fineAmt = FineDAO.getTotalPendingAmount();
        boolean canViewMembers = isMemberOrAdmin();

        JPanel stats = new JPanel(new GridLayout(1, canViewMembers ? 4 : 3, 14, 0));
        stats.setBackground(BG_BLACK);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));
        stats.add(statCard("Total Books", String.valueOf(totalBooks), "All titles in catalog", GOLD, "View catalog",
                "Books"));
        if (canViewMembers) {
            int totalMembers = UserDAO.getTotalCount();
            stats.add(statCard("Members", String.valueOf(totalMembers), "Registered users", WHITE, "View members",
                    "Members"));
        }
        stats.add(statCard("Currently Issued", String.valueOf(issued), "Books checked out", GOLD, overdue + " overdue",
                "Borrow / Return"));
        stats.add(statCard("Fine Dues", "Rs " + (int) fineAmt, "Total pending", RED, overdue + " past deadline",
                "Fines"));

        // Quick actions
        JPanel alLabel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        alLabel.setBackground(BG_BLACK);
        alLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        JLabel ql = new JLabel("Quick Actions");
        ql.setFont(FONT_HEAD);
        ql.setForeground(GOLD);
        alLabel.add(ql);

        boolean isOperator = currentUser.getRole().equals("operator");

        // Dynamic column count
        int cols = isOperator ? 1 : (currentUser.getRole().equals("admin") ? 4 : 3);

        JPanel actions = new JPanel(new GridLayout(1, cols, 14, 0));
        actions.setBackground(BG_BLACK);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // Only admin/non-operator can issue/return
        if (!isOperator) {
            actions.add(actionCard("Issue Book", "Check out a book", RED, "Borrow / Return"));
            actions.add(actionCard("Return Book", "Process a return", GOLD, "Borrow / Return"));
        }

        // Operator + Admin can add books
        if (isMemberOrAdmin()) {
            actions.add(actionCard("Add Book", "Add title to catalog", RED, "Books"));
        }

        // Only admin
        if (currentUser.getRole().equals("admin")) {
            actions.add(actionCard("Monthly Report", "Generate admin report", GOLD, "Reports"));
        }

        dash.add(banner);
        dash.add(Box.createVerticalStrut(20));
        dash.add(stats);
        dash.add(Box.createVerticalStrut(20));
        dash.add(alLabel);
        dash.add(Box.createVerticalStrut(10));
        dash.add(actions);

        contentArea.removeAll();
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_BLACK);
        wrapper.setBorder(new EmptyBorder(24, 24, 24, 24));
        wrapper.add(dash, BorderLayout.NORTH);
        contentArea.add(wrapper, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    // ── Stat card ──────────────────────────────────────────────────────────────
    private JPanel statCard(String label, String value, String sub, Color valColor, String footer,
            String targetSection) {
        JPanel card = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(valColor.equals(RED) ? RED : GOLD);
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 18, 14, 18));

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(MUTED);
        JLabel val = new JLabel(value);
        val.setFont(FONT_NUM);
        val.setForeground(valColor);
        JLabel subL = new JLabel(sub);
        subL.setFont(FONT_SMALL);
        subL.setForeground(MUTED);
        JSeparator s = new JSeparator();
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        s.setForeground(BORDER);
        s.setBackground(BORDER);
        JLabel foot = new JLabel(footer);
        foot.setFont(FONT_SMALL);
        foot.setForeground(valColor.equals(RED) ? RED : GOLD_LIGHT);
        foot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        foot.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                foot.setText("<html><u>" + footer + "</u></html>");
            }

            public void mouseExited(MouseEvent e) {
                foot.setText(footer);
            }

            public void mouseClicked(MouseEvent e) {
                navigateToSection(targetSection);
            }
        });

        card.add(lbl);
        card.add(Box.createVerticalStrut(6));
        card.add(val);
        card.add(subL);
        card.add(Box.createVerticalStrut(10));
        card.add(s);
        card.add(Box.createVerticalStrut(8));
        card.add(foot);
        return card;
    }

    // ── Action card ────────────────────────────────────────────────────────────
    private JPanel actionCard(String title, String sub, Color accent, String targetSection) {
        JPanel card = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JLabel t = new JLabel(title);
        t.setFont(FONT_BOLD);
        t.setForeground(accent);
        JLabel s = new JLabel(sub);
        s.setFont(FONT_SMALL);
        s.setForeground(MUTED);
        card.add(t);
        card.add(Box.createVerticalStrut(5));
        card.add(s);
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                activeSection = targetSection;
                rebuildNav();
                topBarTitle.setText(targetSection);
                showSection(targetSection);
            }

            public void mouseEntered(MouseEvent e) {
                card.setBackground(BG_HOVER);
                card.repaint();
            }

            public void mouseExited(MouseEvent e) {
                card.setBackground(BG_CARD);
                card.repaint();
            }
        });
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════════════
    private boolean isMemberOrAdmin() {
        return currentUser.getRole().equals("operator") || currentUser.getRole().equals("admin");
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty())
            return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1)
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty())
            return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private JButton filledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(fg);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        return btn;
    }

    private void navigateToSection(String section) {
        if (section == null || section.isEmpty())
            return;
        activeSection = section;
        rebuildNav();
        topBarTitle.setText(section);
        showSection(section);
    }

    // ── Legacy no-arg constructor for testing without login ────────────────────
    private void resetDatabase() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "This will delete all books, borrowings, fines, reports, and all non-admin users.\nContinue?",
                "Reset Database", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        if (DatabaseManager.resetDatabase()) {
            bookPanel = null;
            memberPanel = null;
            borrowPanel = null;
            finePanel = null;
            reportPanel = null;
            activeSection = "Dashboard";
            rebuildNav();
            topBarTitle.setText("Dashboard");
            showSection("Dashboard");
            JOptionPane.showMessageDialog(this,
                    "Database reset complete. Admin account was preserved.",
                    "Reset Complete", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Database reset failed.",
                    "Reset Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    public HomePage() {
        this(new User("ADMIN001", "Administrator", "admin@library.com", "", "admin", "admin123"));
    }
}
