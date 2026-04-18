package com.library.ui;

import com.library.dao.UserDAO;
import com.library.model.User;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import static com.library.ThemeColors.*;

public class MemberPanel extends JPanel {

    private final User currentUser;
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;

    public MemberPanel(User currentUser) {
        this.currentUser = currentUser;
        setBackground(BG_BLACK);
        setLayout(new BorderLayout(0, 0));
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
        loadMembers();
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_BLACK);
        bar.setBorder(new EmptyBorder(0, 0, 14, 0));

        searchField = sf("Search by name, ID, email…");
        searchField.setPreferredSize(new Dimension(300, 36));
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { loadMembers(); }
        });

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setBackground(BG_BLACK); left.add(searchField);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setBackground(BG_BLACK);
        if (currentUser.getRole().equals("admin")) {
            JButton add = btn("+ New Member", RED, WHITE);
            JButton edit = btn("Edit Member", BG_CARD, WHITE);
            JButton remove = btn("Remove Member", BG_CARD, WHITE);
            add.addActionListener(e -> showDialog(null));
            edit.addActionListener(e -> editSelected());
            remove.addActionListener(e -> deleteSelected());
            right.add(add);
            right.add(edit);
            right.add(remove);
        }
        bar.add(left, BorderLayout.WEST); bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTable() {
        String[] cols = {"ID","Global ID","Name","Email","Phone","Role","Joined","Status"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        BookPanel.styleTable(table);
        int[] w = {40,100,160,200,110,80,100,80};
        for (int i=0;i<w.length;i++) table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        if (currentUser.getRole().equals("admin")) {
            JPopupMenu pop = new JPopupMenu(); pop.setBackground(BG_CARD);
            JMenuItem edit = mi("Edit");       edit.addActionListener(e -> editSelected());
            JMenuItem act = mi("Activate");   act.addActionListener(e -> setStatus("active"));
            JMenuItem sus = mi("Suspend");     sus.addActionListener(e -> setStatus("suspended"));
            JMenuItem del = mi("Delete");      del.addActionListener(e -> deleteSelected());
            pop.add(edit); pop.add(act); pop.add(sus); pop.addSeparator(); pop.add(del);
            table.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e)  { show(e); }
                public void mouseReleased(MouseEvent e) { show(e); }
                void show(MouseEvent e) {
                    if (!e.isPopupTrigger()) return;
                    int r = table.rowAtPoint(e.getPoint());
                    if (r>=0) table.setRowSelectionInterval(r,r);
                    pop.show(table,e.getX(),e.getY());
                }
            });
        }
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER,1));
        scroll.getViewport().setBackground(BG_CARD);
        return scroll;
    }

    public void loadMembers() {
        model.setRowCount(0);
        String q = getQ();
        for (User u : UserDAO.getAllUsers()) {
            if (u.getRole().equals("admin")) continue;
            if (!q.isEmpty() && !u.getName().toLowerCase().contains(q)
                    && !u.getGlobalId().toLowerCase().contains(q)
                    && (u.getEmail()==null || !u.getEmail().toLowerCase().contains(q))) continue;
            model.addRow(new Object[]{
                u.getId(), u.getGlobalId(), u.getName(),
                nvl(u.getEmail()), nvl(u.getPhone()), u.getRole(),
                nvl(u.getJoinedDate()), u.getStatus()
            });
        }
    }

    private void showDialog(User existing) {
        boolean isEdit = existing != null;
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
            isEdit ? "Edit Member" : "New Member", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(430, isEdit ? 460 : 560); dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(); p.setBackground(BG_CARD);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(24, 28, 24, 28));

        JTextField gid   = df(isEdit ? existing.getGlobalId() : "");
        JTextField name  = df(isEdit ? existing.getName()  : "");
        JTextField email = df(isEdit ? nvl(existing.getEmail()) : "");
        JTextField phone = df(isEdit ? nvl(existing.getPhone()) : "");
        JComboBox<String> role = new JComboBox<>(new String[]{"student","member"});
        if (isEdit) role.setSelectedItem(existing.getRole());
        styleCombo(role);

        p.add(fl("Global ID *")); p.add(gid);   p.add(vs(10));
        p.add(fl("Full Name *")); p.add(name);  p.add(vs(10));
        p.add(fl("Email"));       p.add(email); p.add(vs(10));
        p.add(fl("Phone"));       p.add(phone); p.add(vs(10));
        p.add(fl("Role"));        p.add(role);  p.add(vs(10));

        JPasswordField pass = null;
        if (!isEdit) {
            pass = new JPasswordField();
            pass.setFont(new Font("Segoe UI",Font.PLAIN,13)); pass.setForeground(WHITE);
            pass.setBackground(BG_DARK); pass.setCaretColor(WHITE);
            pass.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER,1,true), new EmptyBorder(6,10,6,10)));
            pass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            pass.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(fl("Password *")); p.add(pass); p.add(vs(10));
        }
        p.add(vs(10));

        final JPasswordField fPass = pass;
        JButton save = btn(isEdit ? "Save" : "Create Member", RED, WHITE);
        save.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        save.setAlignmentX(Component.LEFT_ALIGNMENT);
        save.addActionListener(e -> {
            if (gid.getText().trim().isEmpty() || name.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg,"Global ID and Name required.","Error",JOptionPane.ERROR_MESSAGE); return;
            }
            if (!isEdit && (fPass==null || new String(fPass.getPassword()).trim().isEmpty())) {
                JOptionPane.showMessageDialog(dlg,"Password required.","Error",JOptionPane.ERROR_MESSAGE); return;
            }
            if (!isEdit && UserDAO.globalIdExists(gid.getText().trim())) {
                JOptionPane.showMessageDialog(dlg,"Global ID already in use.","Error",JOptionPane.ERROR_MESSAGE); return;
            }
            User u = new User(gid.getText().trim(), name.getText().trim(), email.getText().trim(),
                phone.getText().trim(), (String)role.getSelectedItem(),
                isEdit ? (existing.getPassword()) : new String(fPass.getPassword()).trim());
            if (isEdit) {
                u.setId(existing.getId());
                u.setStatus(existing.getStatus());
            }
            if (!isEdit) { if (UserDAO.register(u)) { loadMembers(); dlg.dispose(); }
                else JOptionPane.showMessageDialog(dlg,"Failed.","Error",JOptionPane.ERROR_MESSAGE); }
            else if (UserDAO.updateUser(u)) { loadMembers(); dlg.dispose(); }
            else JOptionPane.showMessageDialog(dlg,"Failed to update member.","Error",JOptionPane.ERROR_MESSAGE);
        });
        p.add(save);

        JScrollPane scroll = new JScrollPane(p);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_CARD);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        dlg.setContentPane(scroll); dlg.setVisible(true);
    }

    private void editSelected() {
        int row = table.getSelectedRow(); if (row < 0) return;
        User user = UserDAO.getUserById((int) model.getValueAt(row, 0));
        if (user != null) showDialog(user);
    }

    private void setStatus(String status) {
        int row = table.getSelectedRow(); if (row<0) return;
        if (UserDAO.updateStatus((int)model.getValueAt(row,0), status)) loadMembers();
    }
    private void deleteSelected() {
        int row = table.getSelectedRow(); if (row<0) return;
        int id = (int)model.getValueAt(row,0);
        String name = (String)model.getValueAt(row,2);
        if (JOptionPane.showConfirmDialog(this,"Delete \""+name+"\"?","Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
            if (UserDAO.deleteUser(id)) {
                loadMembers();
                JOptionPane.showMessageDialog(this, "\"" + name + "\" removed from the database.",
                    "Member Removed", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Could not remove the selected member.",
                    "Delete Failed", JOptionPane.ERROR_MESSAGE);
            }
    }

    private String getQ() {
        if (searchField==null) return "";
        String t = searchField.getText().trim();
        return t.equals("Search by name, ID, email…") ? "" : t.toLowerCase();
    }
    private String nvl(String s) { return s!=null&&!s.isEmpty()?s:"—"; }

    private JTextField sf(String ph) {
        JTextField f = new JTextField(ph);
        f.setFont(new Font("Segoe UI",Font.PLAIN,13)); f.setForeground(MUTED);
        f.setBackground(BG_CARD); f.setCaretColor(WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER,1,true),new EmptyBorder(6,12,6,12)));
        f.addFocusListener(new FocusAdapter(){
            public void focusGained(FocusEvent e){if(f.getText().equals(ph)){f.setText("");f.setForeground(WHITE);}}
            public void focusLost(FocusEvent e){if(f.getText().isEmpty()){f.setText(ph);f.setForeground(MUTED);}}
        }); return f;
    }
    private JTextField df(String val) {
        JTextField f = new JTextField(val);
        f.setFont(new Font("Segoe UI",Font.PLAIN,13)); f.setForeground(WHITE);
        f.setBackground(BG_DARK); f.setCaretColor(WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER,1,true),new EmptyBorder(6,10,6,10)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE,36)); f.setAlignmentX(Component.LEFT_ALIGNMENT); return f;
    }
    private JLabel fl(String t) {
        JLabel l=new JLabel(t); l.setFont(new Font("Segoe UI",Font.BOLD,11));
        l.setForeground(MUTED); l.setAlignmentX(Component.LEFT_ALIGNMENT); return l;
    }
    private void styleCombo(JComboBox<String> c) {
        c.setFont(new Font("Segoe UI",Font.PLAIN,13)); c.setBackground(BG_DARK); c.setForeground(WHITE);
        c.setBorder(BorderFactory.createLineBorder(BORDER,1));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE,36)); c.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
    private JMenuItem mi(String t) {
        JMenuItem i=new JMenuItem(t); i.setBackground(BG_CARD); i.setForeground(WHITE);
        i.setFont(new Font("Segoe UI",Font.PLAIN,12)); return i;
    }
    private Component vs(int h) { return Box.createVerticalStrut(h); }
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
