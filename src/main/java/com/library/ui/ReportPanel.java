package com.library.ui;

import com.library.dao.*;
import com.library.db.DatabaseManager;
import com.library.model.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.library.ThemeColors.*;

public class ReportPanel extends JPanel {

    private final User currentUser;
    private JTextArea  area;
    private JComboBox<String> monthCombo;

    public ReportPanel(User currentUser) {
        this.currentUser = currentUser;
        setBackground(BG_BLACK);
        setLayout(new BorderLayout(0, 14));
        add(buildBar(),  BorderLayout.NORTH);
        add(buildArea(), BorderLayout.CENTER);
    }

    private JPanel buildBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_BLACK);

        String[] months = new String[12];
        LocalDate now = LocalDate.now();
        for (int i=0;i<12;i++)
            months[i] = now.minusMonths(i).format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        monthCombo = new JComboBox<>(months);
        monthCombo.setFont(new Font("Segoe UI",Font.PLAIN,13));
        monthCombo.setBackground(BG_DARK); monthCombo.setForeground(WHITE);
        monthCombo.setBorder(BorderFactory.createLineBorder(BORDER,1));
        monthCombo.setPreferredSize(new Dimension(180,36));

        JLabel lbl = new JLabel("Month: ");
        lbl.setFont(new Font("Segoe UI",Font.BOLD,12)); lbl.setForeground(MUTED);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setBackground(BG_BLACK); left.add(lbl); left.add(monthCombo);

        JButton gen   = btn("Generate Report", RED, WHITE);
        JButton clear = btn("Clear",           BG_CARD, MUTED);
        gen.addActionListener(e   -> generate());
        clear.addActionListener(e -> area.setText(""));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        right.setBackground(BG_BLACK); right.add(gen); right.add(clear);

        bar.add(left, BorderLayout.WEST); bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildArea() {
        area = new JTextArea("Select a month and click Generate Report.");
        area.setEditable(false); area.setBackground(BG_CARD); area.setForeground(WHITE);
        area.setFont(new Font("Consolas",Font.PLAIN,13));
        area.setBorder(new EmptyBorder(16,16,16,16));
        JScrollPane s = new JScrollPane(area);
        s.setBorder(BorderFactory.createLineBorder(BORDER,1));
        s.getViewport().setBackground(BG_CARD);
        return s;
    }

    private void generate() {
        String month = (String) monthCombo.getSelectedItem();
        String line  = "═".repeat(62);
        String thin  = "─".repeat(62);
        StringBuilder sb = new StringBuilder();

        sb.append(line).append("\n");
        sb.append("  LIBRARY MANAGEMENT SYSTEM — MONTHLY REPORT\n");
        sb.append("  Month     : ").append(month).append("\n");
        sb.append("  Generated : ").append(LocalDate.now()).append("\n");
        sb.append(line).append("\n\n");

        int totalBooks   = BookDAO.getTotalCount();
        int issued       = BookDAO.getIssuedCount();
        int overdue      = BookDAO.getOverdueCount();
        int totalMembers = UserDAO.getTotalCount();
        double pending   = FineDAO.getTotalPendingAmount();

        sb.append("  SUMMARY\n").append(thin).append("\n");
        sb.append(row("Total books in catalog:",   String.valueOf(totalBooks)));
        sb.append(row("Currently issued:",         String.valueOf(issued)));
        sb.append(row("Overdue books:",            String.valueOf(overdue)));
        sb.append(row("Registered members:",       String.valueOf(totalMembers)));
        sb.append(row("Total pending fines:",      "₹ " + (int) pending));
        sb.append("\n");

        List<Borrowing> active = BorrowingDAO.getActiveBorrowings();
        sb.append("  ACTIVE BORROWINGS (").append(active.size()).append(")\n").append(thin).append("\n");
        if (active.isEmpty()) { sb.append("  None.\n"); }
        else {
            sb.append(String.format("  %-4s %-30s %-20s %-12s %-12s%n","ID","Book","Member","Issued","Due"));
            for (Borrowing b : active)
                sb.append(String.format("  %-4d %-30s %-20s %-12s %-12s%n",
                    b.getId(), trunc(b.getBookTitle(),28), trunc(b.getUserName(),18),
                    b.getBorrowDate(), b.getDueDate()));
        }
        sb.append("\n");

        List<Borrowing> od = BorrowingDAO.getOverdueBorrowings();
        sb.append("  OVERDUE BORROWINGS (").append(od.size()).append(")\n").append(thin).append("\n");
        if (od.isEmpty()) { sb.append("  None.\n"); }
        else {
            sb.append(String.format("  %-4s %-28s %-18s %-12s %-12s%n","ID","Book","Member","Due","Fine"));
            for (Borrowing b : od)
                sb.append(String.format("  %-4d %-28s %-18s %-12s ₹ %-8.0f%n",
                    b.getId(), trunc(b.getBookTitle(),26), trunc(b.getUserName(),16),
                    b.getDueDate(), b.getFine()));
        }
        sb.append("\n");

        List<Fine> fines = FineDAO.getPendingFines();
        sb.append("  PENDING FINE DUES (").append(fines.size()).append(")\n").append(thin).append("\n");
        if (fines.isEmpty()) { sb.append("  No pending fines.\n"); }
        else {
            sb.append(String.format("  %-4s %-20s %-28s %-12s%n","ID","Member","Book","Amount"));
            for (Fine f : fines)
                sb.append(String.format("  %-4d %-20s %-28s ₹ %-8.0f%n",
                    f.getId(), trunc(f.getUserName(),18), trunc(f.getBookTitle(),26), f.getAmount()));
        }
        sb.append("\n").append(line).append("\n  END OF REPORT\n").append(line).append("\n");

        area.setText(sb.toString());
        area.setCaretPosition(0);
        saveReport(month, active.size(), pending, sb.toString());
    }

    private String row(String label, String val) {
        return String.format("  %-35s %s%n", label, val);
    }
    private String trunc(String s, int max) {
        if (s==null) return "—";
        return s.length()>max ? s.substring(0,max-1)+"…" : s;
    }
    private void saveReport(String month, int issued, double fines, String content) {
        String sql = "INSERT INTO reports (month,total_issued,total_fines,content) VALUES (?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1,month); ps.setInt(2,issued);
            ps.setDouble(3,fines); ps.setString(4,content);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[Report] Save: " + e.getMessage()); }
    }
    private JButton btn(String text, Color bg, Color fg) {
        JButton b=new JButton(text){
            protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()?bg.brighter():bg);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI",Font.BOLD,12)); b.setForeground(fg);
        b.setOpaque(false); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8,16,8,16)); return b;
    }
}