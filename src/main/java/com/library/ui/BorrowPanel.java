package com.library.ui;

import com.library.dao.BookDAO;
import com.library.dao.BorrowingDAO;
import com.library.dao.UserDAO;
import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.User;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.library.ThemeColors.BG_BLACK;
import static com.library.ThemeColors.BG_CARD;
import static com.library.ThemeColors.BG_DARK;
import static com.library.ThemeColors.BORDER;
import static com.library.ThemeColors.GOLD;
import static com.library.ThemeColors.MUTED;
import static com.library.ThemeColors.RED;
import static com.library.ThemeColors.WHITE;

public class BorrowPanel extends JPanel {

    private static final Color TAB_ACTIVE_BG = new Color(21, 55, 92);
    private static final Color TAB_INACTIVE_BG = BG_DARK;
    private static final Color TAB_BORDER = new Color(90, 126, 168);

    private final User currentUser;
    private JTable activeTable, historyTable;
    private DefaultTableModel activeModel, historyModel;
    private JTabbedPane tabs;

    public BorrowPanel(User currentUser) {
        this.currentUser = currentUser;
        setBackground(BG_BLACK);
        setLayout(new BorderLayout(0, 0));

        JPanel toolbar = buildToolbar();
        add(toolbar, BorderLayout.NORTH);

        tabs = new JTabbedPane();
        tabs.setBackground(BG_DARK);
        tabs.setForeground(WHITE);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabs.setFocusable(false);

        tabs.addTab("Active Borrowings", buildActiveTab());
        tabs.addTab("All History", buildHistoryTab());
        styleTabs();

        add(tabs, BorderLayout.CENTER);
        loadData();
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setBackground(BG_BLACK);
        bar.setBorder(new EmptyBorder(0, 0, 16, 0));

        JButton issueBtn = filledButton("Issue Book", RED, WHITE);
        JButton returnBtn = filledButton("Return Book", GOLD, BG_BLACK);
        JButton refreshBtn = filledButton("Refresh", BG_CARD, MUTED);

        issueBtn.addActionListener(e -> showIssueDialog());
        returnBtn.addActionListener(e -> returnSelected());
        refreshBtn.addActionListener(e -> loadData());

        bar.add(issueBtn);
        bar.add(Box.createHorizontalStrut(10));
        bar.add(returnBtn);
        bar.add(Box.createHorizontalStrut(10));
        bar.add(refreshBtn);
        return bar;
    }

    private JScrollPane buildActiveTab() {
        String[] cols = {"ID", "Book", "Member", "Issued On", "Due Date", "Days Left", "Fine (est.)", "Status"};
        activeModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        activeTable = new JTable(activeModel);
        BookPanel.styleTable(activeTable);
        activeTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        activeTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        activeTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        activeTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        activeTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        activeTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        activeTable.getColumnModel().getColumn(6).setPreferredWidth(90);
        activeTable.getColumnModel().getColumn(7).setPreferredWidth(80);

        JScrollPane scroll = new JScrollPane(activeTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scroll.getViewport().setBackground(BG_CARD);
        return scroll;
    }

    private JScrollPane buildHistoryTab() {
        String[] cols = {"ID", "Book", "Member", "Issued On", "Due Date", "Returned On", "Fine", "Status"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(historyModel);
        BookPanel.styleTable(historyTable);
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(190);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        historyTable.getColumnModel().getColumn(7).setPreferredWidth(80);

        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scroll.getViewport().setBackground(BG_CARD);
        return scroll;
    }

    public void loadData() {
        loadActive();
        loadHistory();
    }

    private void loadActive() {
        activeModel.setRowCount(0);
        List<Borrowing> list = BorrowingDAO.getActiveBorrowings();
        for (Borrowing b : list) {
            long daysLeft = daysUntil(b.getDueDate());
            double fine = BorrowingDAO.calculateFine(b.getDueDate());
            String status = daysLeft < 0 ? "Overdue" : "Active";
            activeModel.addRow(new Object[]{
                b.getId(), b.getBookTitle(), b.getUserName(),
                b.getBorrowDate(), b.getDueDate(),
                daysLeft < 0 ? "OVERDUE" : daysLeft + " days",
                fine > 0 ? "Rs " + (int) fine : "None",
                status
            });
        }
    }

    private void loadHistory() {
        historyModel.setRowCount(0);
        List<Borrowing> list = BorrowingDAO.getAllBorrowings();
        for (Borrowing b : list) {
            historyModel.addRow(new Object[]{
                b.getId(), b.getBookTitle(), b.getUserName(),
                b.getBorrowDate(), b.getDueDate(),
                b.getReturnDate() != null ? b.getReturnDate() : "-",
                b.getFine() > 0 ? "Rs " + (int) b.getFine() : "None",
                b.getStatus()
            });
        }
    }

    private void showIssueDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
            "Issue Book", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(760, 520);
        dialog.setLocationRelativeTo(this);

        List<Book> books = BookDAO.getAvailableBooks();
        List<User> members = UserDAO.getMembers();

        if (books.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No books are currently available.", "No Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (members.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No eligible members are registered.", "No Members", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new BorderLayout(18, 18));
        panel.setBackground(BG_CARD);
        panel.setBorder(new EmptyBorder(22, 24, 22, 24));

        JTextField searchField = dialogField("");
        searchField.setToolTipText("Search by title, author, ISBN, or genre");
        JComboBox<String> genreFilter = new JComboBox<>(buildGenreOptions(books));
        styleCombo(genreFilter);

        DefaultListModel<Book> bookModel = new DefaultListModel<>();
        JList<Book> bookList = new JList<>(bookModel);
        bookList.setCellRenderer(new BookListRenderer());
        bookList.setBackground(BG_DARK);
        bookList.setSelectionBackground(RED.darker());
        bookList.setSelectionForeground(WHITE);
        bookList.setFixedCellHeight(60);
        bookList.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        JScrollPane bookScroll = new JScrollPane(bookList);
        bookScroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        bookScroll.getViewport().setBackground(BG_DARK);

        JComboBox<Object> memberCombo = new JComboBox<>();
        memberCombo.addItem("Select member");
        for (User member : members) memberCombo.addItem(member);
        styleCombo(memberCombo);
        memberCombo.setSelectedIndex(0);

        JLabel helper = new JLabel("Search books by title, author, ISBN, or genre, then narrow by genre.");
        helper.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        helper.setForeground(MUTED);

        JLabel resultCount = new JLabel();
        resultCount.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        resultCount.setForeground(MUTED);

        Runnable applyFilters = () -> {
            String search = searchField.getText().trim().toLowerCase();
            String selectedGenre = (String) genreFilter.getSelectedItem();

            List<Book> filtered = books.stream()
                .filter(book -> matchesGenre(book, selectedGenre))
                .filter(book -> matchesSearch(book, search))
                .collect(Collectors.toList());

            bookModel.clear();
            filtered.forEach(bookModel::addElement);
            if (!filtered.isEmpty()) {
                bookList.setSelectedIndex(0);
            }
            resultCount.setText(filtered.size() + " book" + (filtered.size() == 1 ? "" : "s") + " found");
        };

        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { applyFilters.run(); }
        });
        genreFilter.addActionListener(e -> applyFilters.run());
        applyFilters.run();

        JLabel dueLbl = new JLabel("Due date: 28 days from today");
        dueLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dueLbl.setForeground(GOLD);
        dueLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton issueBtn = filledButton("Issue Book", RED, WHITE);
        issueBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        issueBtn.addActionListener(e -> {
            Book book = bookList.getSelectedValue();
            Object memberValue = memberCombo.getSelectedItem();
            User member = memberValue instanceof User ? (User) memberValue : null;

            if (book == null) {
                JOptionPane.showMessageDialog(dialog,
                    "Select a book to issue.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (member == null) {
                JOptionPane.showMessageDialog(dialog,
                    "Select a valid member.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (BorrowingDAO.hasActiveBorrowing(book.getId(), member.getId())) {
                JOptionPane.showMessageDialog(dialog,
                    "\"" + book.getTitle() + "\" is already issued to " + member.getName() + ".",
                    "Duplicate Issue Blocked", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (BorrowingDAO.issueBook(book.getId(), member.getId())) {
                JOptionPane.showMessageDialog(dialog,
                    "\"" + book.getTitle() + "\" issued to " + member.getName() + ".\nDue in 28 days.",
                    "Issued", JOptionPane.INFORMATION_MESSAGE);
                loadData();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "Failed to issue book. Check member validity and book availability.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(dlgLabel("Find Book"));
        top.add(Box.createVerticalStrut(6));

        JPanel filterRow = new JPanel(new GridLayout(1, 2, 10, 0));
        filterRow.setOpaque(false);
        filterRow.add(searchField);
        filterRow.add(genreFilter);

        top.add(filterRow);
        top.add(Box.createVerticalStrut(8));
        top.add(helper);
        top.add(Box.createVerticalStrut(6));
        top.add(resultCount);

        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setOpaque(false);
        left.add(top, BorderLayout.NORTH);
        left.add(bookScroll, BorderLayout.CENTER);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(240, 0));
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.add(dlgLabel("Select Member"));
        right.add(Box.createVerticalStrut(6));
        right.add(memberCombo);
        right.add(Box.createVerticalStrut(14));
        right.add(dlgLabel("Issue Details"));
        right.add(Box.createVerticalStrut(6));
        right.add(infoCard("Loan period", "28 days"));
        right.add(Box.createVerticalStrut(8));
        right.add(infoCard("Fine policy", "Rs 10/day after due date"));
        right.add(Box.createVerticalStrut(8));
        right.add(infoCard("Validation", "Placeholder members and duplicate active issues are blocked"));
        right.add(Box.createVerticalGlue());
        right.add(dueLbl);
        right.add(Box.createVerticalStrut(14));
        right.add(issueBtn);

        panel.add(left, BorderLayout.CENTER);
        panel.add(right, BorderLayout.EAST);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void returnSelected() {
        if (tabs.getSelectedIndex() != 0) {
            JOptionPane.showMessageDialog(this,
                "Select a borrowing from the Active tab to return.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int row = activeTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Select a row in Active Borrowings to return.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) activeModel.getValueAt(row, 0);
        String book = (String) activeModel.getValueAt(row, 1);
        String fineStr = (String) activeModel.getValueAt(row, 6);

        String msg = "Return \"" + book + "\"?";
        if (!fineStr.equals("None")) msg += "\nFine: " + fineStr + " will be recorded.";

        int confirm = JOptionPane.showConfirmDialog(this, msg, "Confirm Return",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        if (BorrowingDAO.returnBook(id)) {
            JOptionPane.showMessageDialog(this, "Book returned successfully.", "Returned", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to process return.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private long daysUntil(String dateStr) {
        try {
            java.time.LocalDate due = java.time.LocalDate.parse(dateStr);
            java.time.LocalDate today = java.time.LocalDate.now();
            return due.toEpochDay() - today.toEpochDay();
        } catch (Exception e) {
            return 0;
        }
    }

    private JLabel dlgLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField dialogField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(WHITE);
        field.setBackground(BG_DARK);
        field.setCaretColor(WHITE);
        field.setBorder(fieldBorder());
        return field;
    }

    private String[] buildGenreOptions(List<Book> books) {
        Set<String> genres = new LinkedHashSet<>();
        genres.add("All genres");
        for (Book book : books) {
            String genre = book.getGenre() == null ? "" : book.getGenre().trim();
            if (!genre.isEmpty()) genres.add(genre);
        }
        return genres.toArray(new String[0]);
    }

    private boolean matchesSearch(Book book, String search) {
        if (search.isEmpty()) return true;
        return safe(book.getTitle()).contains(search)
            || safe(book.getAuthor()).contains(search)
            || safe(book.getIsbn()).contains(search)
            || safe(book.getGenre()).contains(search);
    }

    private boolean matchesGenre(Book book, String genre) {
        if (genre == null || genre.equals("All genres")) return true;
        return genre.equalsIgnoreCase(book.getGenre());
    }

    private String safe(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private JPanel infoCard(String title, String body) {
        JPanel card = new JPanel();
        card.setBackground(BG_DARK);
        card.setBorder(new CompoundBorder(fieldBorder(), new EmptyBorder(10, 12, 10, 12)));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(GOLD);

        JLabel bodyLabel = new JLabel("<html><body style='width:180px'>" + body + "</body></html>");
        bodyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bodyLabel.setForeground(WHITE);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(3));
        card.add(bodyLabel);
        return card;
    }

    private Border fieldBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true), new EmptyBorder(8, 10, 8, 10));
    }

    private <T> void styleCombo(JComboBox<T> c) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.setBackground(BG_DARK);
        c.setForeground(WHITE);
        c.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JButton filledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
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
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    private void styleTabs() {
        tabs.setOpaque(false);
        tabs.setBackground(BG_BLACK);
        tabs.setForeground(WHITE);
        tabs.setBorder(new EmptyBorder(0, 0, 0, 0));
        UIManager.put("TabbedPane.selected", TAB_ACTIVE_BG);
        UIManager.put("TabbedPane.contentAreaColor", BG_CARD);
        UIManager.put("TabbedPane.focus", TAB_ACTIVE_BG);
        refreshTabHeaders();
        tabs.addChangeListener(e -> refreshTabHeaders());
    }

    private void refreshTabHeaders() {
        for (int i = 0; i < tabs.getTabCount(); i++) {
            boolean selected = i == tabs.getSelectedIndex();
            tabs.setTabComponentAt(i, createTabLabel(tabs.getTitleAt(i), selected));
        }
    }

    private Component createTabLabel(String title, boolean selected) {
        JLabel label = new JLabel(title);
        label.setOpaque(true);
        label.setForeground(WHITE);
        label.setBackground(selected ? TAB_ACTIVE_BG : TAB_INACTIVE_BG);
        label.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(selected ? TAB_BORDER : BORDER, 1),
            new EmptyBorder(6, 14, 6, 14)));
        label.setFont(new Font("Segoe UI", selected ? Font.BOLD : Font.PLAIN, 12));
        return label;
    }

    private static class BookListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JPanel row = new JPanel(new BorderLayout(8, 4));
            row.setBorder(new EmptyBorder(8, 10, 8, 10));
            row.setOpaque(true);
            row.setBackground(isSelected ? RED.darker() : BG_DARK);

            if (value instanceof Book book) {
                JLabel title = new JLabel(book.getTitle());
                title.setFont(new Font("Segoe UI", Font.BOLD, 13));
                title.setForeground(WHITE);

                String genre = (book.getGenre() == null || book.getGenre().isBlank()) ? "General" : book.getGenre();
                JLabel meta = new JLabel(book.getAuthor() + "  |  " + genre + "  |  " + book.getAvailableCopies() + " available");
                meta.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                meta.setForeground(isSelected ? WHITE : MUTED);

                row.add(title, BorderLayout.NORTH);
                row.add(meta, BorderLayout.SOUTH);
            }
            return row;
        }
    }
}
