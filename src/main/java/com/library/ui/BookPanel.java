package com.library.ui;

import com.library.dao.BookDAO;
import com.library.model.Book;
import com.library.model.User;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import static com.library.ThemeColors.*;

public class BookPanel extends JPanel {

    private final User currentUser;
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private JLabel countLabel;

    public BookPanel(User currentUser) {
        this.currentUser = currentUser;
        setBackground(BG_BLACK);
        setLayout(new BorderLayout(0, 0));
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
        loadBooks(null);
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_BLACK);
        bar.setBorder(new EmptyBorder(0, 0, 14, 0));

        searchField = styledField("Search title, author, ISBN, genre…");
        searchField.setPreferredSize(new Dimension(300, 36));
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { loadBooks(getText()); }
        });

        countLabel = new JLabel("Loading…");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setForeground(MUTED);
        countLabel.setBorder(new EmptyBorder(0, 14, 0, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setBackground(BG_BLACK);
        left.add(searchField); left.add(countLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setBackground(BG_BLACK);
        if (canEdit()) {
            JButton add = btn("+ Add Book", RED, WHITE);
            add.addActionListener(e -> showDialog(null));
            right.add(add);
        }
        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTable() {
        String[] cols = {"ID","Title","Author","ISBN","Genre","Year","Total","Available","Status"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        styleTable(table);
        int[] widths = {40,220,140,110,90,50,50,70,80};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(BG_CARD);
        if (canEdit()) {
            JMenuItem edit = mi("Edit Book");   edit.addActionListener(e -> editSelected());
            JMenuItem del  = mi("Remove Book"); del.addActionListener(e -> deleteSelected());
            popup.add(edit); popup.add(del); popup.addSeparator();
        }
        JMenuItem issue = mi("Issue this Book");
        issue.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "Go to Borrow / Return to issue this book.", "Issue Book", JOptionPane.INFORMATION_MESSAGE));
        popup.add(issue);

        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e)  { pop(e); }
            public void mouseReleased(MouseEvent e) { pop(e); }
            void pop(MouseEvent e) {
                if (!e.isPopupTrigger()) return;
                int r = table.rowAtPoint(e.getPoint());
                if (r >= 0) table.setRowSelectionInterval(r, r);
                popup.show(table, e.getX(), e.getY());
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scroll.getViewport().setBackground(BG_CARD);
        return scroll;
    }

    public void loadBooks(String query) {
        model.setRowCount(0);
        List<Book> books = (query == null || query.isEmpty())
            ? BookDAO.getAllBooks() : BookDAO.searchBooks(query);
        for (Book b : books) {
            model.addRow(new Object[]{
                b.getId(), b.getTitle(), b.getAuthor(),
                nvl(b.getIsbn()), nvl(b.getGenre()),
                b.getYear() > 0 ? b.getYear() : "—",
                b.getTotalCopies(), b.getAvailableCopies(),
                b.isAvailable() ? "Available" : "All Out"
            });
        }
        countLabel.setText(books.size() + " book" + (books.size() == 1 ? "" : "s"));
    }

    private void editSelected() {
        int row = table.getSelectedRow(); if (row < 0) return;
        Book b = BookDAO.getBookById((int) model.getValueAt(row, 0));
        if (b != null) showDialog(b);
    }

    private void showDialog(Book existing) {
        boolean isEdit = existing != null;
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
            isEdit ? "Edit Book" : "Add Book", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(440, 540); dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(); p.setBackground(BG_CARD);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(24, 28, 24, 28));

        JTextField title     = df(isEdit ? existing.getTitle()     : "");
        JTextField author    = df(isEdit ? existing.getAuthor()     : "");
        JTextField isbn      = df(isEdit ? nvl(existing.getIsbn())  : "");
        JTextField genre     = df(isEdit ? nvl(existing.getGenre()) : "");
        JTextField publisher = df(isEdit ? nvl(existing.getPublisher()) : "");
        JTextField year      = df(isEdit && existing.getYear()>0 ? String.valueOf(existing.getYear()) : "");
        JTextField copies    = df(isEdit ? String.valueOf(existing.getTotalCopies()) : "1");

        p.add(fl("Title *"));     p.add(title);     p.add(vs(10));
        p.add(fl("Author *"));    p.add(author);    p.add(vs(10));
        p.add(fl("ISBN"));        p.add(isbn);      p.add(vs(10));
        p.add(fl("Genre"));       p.add(genre);     p.add(vs(10));
        p.add(fl("Publisher"));   p.add(publisher); p.add(vs(10));

        JPanel row = new JPanel(new GridLayout(1,2,10,0));
        row.setOpaque(false); row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        row.add(miniCol("Year", year)); row.add(miniCol("Copies", copies));
        p.add(row); p.add(vs(20));

        JButton save = btn(isEdit ? "Save Changes" : "Add Book", RED, WHITE);
        save.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        save.setAlignmentX(Component.LEFT_ALIGNMENT);
        save.addActionListener(e -> {
            if (title.getText().trim().isEmpty() || author.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Title and Author are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Book b = isEdit ? existing : new Book();
            b.setTitle(title.getText().trim()); b.setAuthor(author.getText().trim());
            b.setIsbn(isbn.getText().trim());   b.setGenre(genre.getText().trim());
            b.setPublisher(publisher.getText().trim());
            try { b.setYear(Integer.parseInt(year.getText().trim())); } catch (NumberFormatException ex) { b.setYear(0); }
            try { b.setTotalCopies(Integer.parseInt(copies.getText().trim())); } catch (NumberFormatException ex) { b.setTotalCopies(1); }

            boolean ok = isEdit ? BookDAO.updateBook(b) : BookDAO.addBook(b);
            if (ok) { loadBooks(null); dlg.dispose(); }
            else JOptionPane.showMessageDialog(dlg, "Failed. Check if ISBN is unique.", "Error", JOptionPane.ERROR_MESSAGE);
        });
        p.add(save);
        dlg.setContentPane(p); dlg.setVisible(true);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow(); if (row < 0) return;
        int id = (int) model.getValueAt(row, 0);
        String title = (String) model.getValueAt(row, 1);
        if (JOptionPane.showConfirmDialog(this, "Remove \"" + title + "\"?", "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (BookDAO.deleteBook(id)) loadBooks(null);
            else JOptionPane.showMessageDialog(this,
                "Cannot remove — book may have active borrowings.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Shared table styling (used by other panels) ────────────────────────────
    public static void styleTable(JTable t) {
        t.setBackground(BG_CARD); t.setForeground(WHITE);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setRowHeight(34); t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(192, 39, 45, 60));
        t.setSelectionForeground(WHITE); t.setFillsViewportHeight(true);
        t.getTableHeader().setBackground(BG_DARK); t.getTableHeader().setForeground(GOLD);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        t.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        t.getTableHeader().setReorderingAllowed(false);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                setBackground(sel ? new Color(192,39,45,60) : row%2==0 ? BG_CARD : new Color(32,32,32));
                setForeground(WHITE);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                String sv = val != null ? val.toString() : "";
                if (sv.equals("Available") || sv.equals("returned") || sv.equals("active"))
                    setForeground(GREEN);
                else if (sv.equals("All Out") || sv.equals("overdue") || sv.equals("Overdue"))
                    setForeground(RED);
                else if (sv.equals("suspended"))
                    setForeground(GOLD);
                return this;
            }
        });
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private boolean canEdit() {
        return currentUser != null &&
            (currentUser.getRole().equals("operator") || currentUser.getRole().equals("admin"));
    }
    private String getText() {
        String t = searchField.getText().trim();
        return t.equals("Search title, author, ISBN, genre…") ? "" : t;
    }
    private String nvl(String s) { return s != null && !s.isEmpty() ? s : "—"; }

    private JTextField styledField(String ph) {
        JTextField f = new JTextField(ph);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13)); f.setForeground(MUTED);
        f.setBackground(BG_CARD); f.setCaretColor(WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER,1,true), new EmptyBorder(6,12,6,12)));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { if (f.getText().equals(ph)) { f.setText(""); f.setForeground(WHITE); } }
            public void focusLost(FocusEvent e)   { if (f.getText().isEmpty())  { f.setText(ph); f.setForeground(MUTED); } }
        });
        return f;
    }
    private JTextField df(String val) {
        JTextField f = new JTextField(val);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13)); f.setForeground(WHITE);
        f.setBackground(BG_DARK); f.setCaretColor(WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER,1,true), new EmptyBorder(6,10,6,10)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        f.setAlignmentX(Component.LEFT_ALIGNMENT); return f;
    }
    private JLabel fl(String t) {
        JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(MUTED); l.setAlignmentX(Component.LEFT_ALIGNMENT); return l;
    }
    private JPanel miniCol(String label, JComponent field) {
        JPanel p = new JPanel(); p.setOpaque(false); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(fl(label)); p.add(Box.createVerticalStrut(4)); p.add(field); return p;
    }
    private Component vs(int h) { return Box.createVerticalStrut(h); }
    private JMenuItem mi(String t) {
        JMenuItem i = new JMenuItem(t); i.setBackground(BG_CARD); i.setForeground(WHITE);
        i.setFont(new Font("Segoe UI", Font.PLAIN, 12)); return i;
    }
    private JButton btn(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12)); b.setForeground(fg);
        b.setOpaque(false); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8,16,8,16)); return b;
    }
}
