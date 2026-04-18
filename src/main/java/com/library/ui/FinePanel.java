package com.library.ui;

import com.library.dao.FineDAO;
import com.library.model.Fine;
import com.library.model.User;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

import static com.library.ThemeColors.*;

public class FinePanel extends JPanel {

    private final User currentUser;
    private javax.swing.table.DefaultTableModel model;
    private JTable table;
    private JLabel totalLabel;

    public FinePanel(User currentUser) {
        this.currentUser = currentUser;
        setBackground(BG_BLACK);
        setLayout(new BorderLayout(0, 14));
        add(buildBar(),   BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        loadFines();
    }

    private JPanel buildBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_BLACK);

        totalLabel = new JLabel("Total pending: ₹ 0");
        totalLabel.setFont(new Font("Georgia", Font.BOLD, 16));
        totalLabel.setForeground(GOLD_LIGHT);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setBackground(BG_BLACK); left.add(totalLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setBackground(BG_BLACK);

        JButton pending  = btn("Pending Only", RED,    WHITE);
        JButton all      = btn("Show All",     BG_CARD, MUTED);
        JButton paid     = btn("Mark as Paid", GOLD,   BG_BLACK);
        JButton refresh  = btn("↻ Refresh",   BG_CARD, MUTED);

        pending.addActionListener(e  -> loadPending());
        all.addActionListener(e      -> loadFines());
        paid.addActionListener(e     -> markPaid());
        refresh.addActionListener(e  -> loadFines());

        right.add(pending); right.add(all); right.add(paid); right.add(refresh);
        bar.add(left, BorderLayout.WEST); bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTable() {
        String[] cols = {"Fine ID","Member","Book","Amount","Due Date","Paid","Paid On"};
        model = new javax.swing.table.DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        BookPanel.styleTable(table);
        int[] w = {60,150,200,80,100,60,100};
        for (int i=0;i<w.length;i++) table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scroll.getViewport().setBackground(BG_CARD);
        return scroll;
    }

    public void loadFines() { populate(FineDAO.getAllFines()); }
    public void loadPending() { populate(FineDAO.getPendingFines()); }

    private void populate(List<Fine> fines) {
        model.setRowCount(0);
        double total = 0;
        for (Fine f : fines) {
            model.addRow(new Object[]{
                f.getId(), f.getUserName(), f.getBookTitle(),
                "₹ " + (int) f.getAmount(),
                f.getDueDate() != null ? f.getDueDate() : "—",
                f.isPaid() ? "Yes" : "No",
                f.getPaidDate() != null ? f.getPaidDate() : "—"
            });
            if (!f.isPaid()) total += f.getAmount();
        }
        totalLabel.setText("Total pending: ₹ " + (int) total);
    }

    private void markPaid() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,"Select a fine row first.","No Selection",JOptionPane.WARNING_MESSAGE); return;
        }
        if ("Yes".equals(model.getValueAt(row, 5))) {
            JOptionPane.showMessageDialog(this,"Already paid.","Info",JOptionPane.INFORMATION_MESSAGE); return;
        }
        int id = (int) model.getValueAt(row, 0);
        String member = (String) model.getValueAt(row, 1);
        String amount = (String) model.getValueAt(row, 3);
        if (JOptionPane.showConfirmDialog(this,"Mark "+amount+" for "+member+" as paid?",
                "Confirm Payment", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (FineDAO.markAsPaid(id)) {
                JOptionPane.showMessageDialog(this,"Fine marked as paid.","Success",JOptionPane.INFORMATION_MESSAGE);
                loadFines();
            }
        }
    }

    private JButton btn(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            protected void paintComponent(Graphics g) {
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