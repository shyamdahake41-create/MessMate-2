package com.messmate.ui;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JLabel pageTitle = new JLabel("Dashboard");
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private final DashboardPanel dashboard = new DashboardPanel();
    private final MembersPanel members = new MembersPanel();
    private final MembershipsPanel memberships = new MembershipsPanel();
    private final PaymentsPanel payments = new PaymentsPanel();

    public MainFrame(String username) {
        super("MessMate - Mess Membership Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 700));
        setSize(1200, 760);
        setLocationRelativeTo(null);

        content.setBackground(Ui.BG);
        content.add(dashboard, "dashboard");
        content.add(members, "members");
        content.add(memberships, "memberships");
        content.add(payments, "payments");

        JPanel root = new JPanel(new BorderLayout());
        root.add(sidebar(), BorderLayout.WEST);
        root.add(body(username), BorderLayout.CENTER);
        setContentPane(root);
        showPage("dashboard", "Dashboard");
    }

    private JPanel sidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Ui.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(240, 0));

        JPanel brand = new JPanel(new BorderLayout(0, 4));
        brand.setOpaque(false);
        brand.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 20, 20, 20));
        JLabel title = new JLabel("MessMate");
        title.setForeground(Color.WHITE);
        title.setFont(Ui.FONT.deriveFont(Font.BOLD, 26f));
        JLabel tag = new JLabel("Membership desk");
        tag.setForeground(new Color(0xD5E4E2));
        tag.setFont(Ui.FONT.deriveFont(12f));
        brand.add(title, BorderLayout.NORTH);
        brand.add(tag, BorderLayout.SOUTH);

        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setOpaque(false);
        nav.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 16, 16));
        addNav(nav, "dashboard", "Dashboard");
        addNav(nav, "members", "Members");
        addNav(nav, "memberships", "Memberships");
        addNav(nav, "payments", "Payments");

        JButton logout = Ui.navButton("Logout");
        if (logout instanceof FlatButton flat) {
            flat.setFill(new Color(0x8C3A32));
        }
        logout.addActionListener(event -> logout());
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 20, 16));
        bottom.add(logout, BorderLayout.CENTER);

        sidebar.add(brand, BorderLayout.NORTH);
        sidebar.add(nav, BorderLayout.CENTER);
        sidebar.add(bottom, BorderLayout.SOUTH);
        return sidebar;
    }

    private void addNav(JPanel nav, String key, String title) {
        JButton button = Ui.navButton(title);
        if (button instanceof FlatButton flat) {
            flat.setFill(new Color(0x184852));
        }
        button.addActionListener(event -> showPage(key, title));
        navButtons.put(key, button);
        nav.add(button);
        nav.add(Box.createVerticalStrut(8));
    }

    private JPanel body(String username) {
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(Ui.BG);
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 20, 4, 20));
        pageTitle.setFont(Ui.FONT.deriveFont(Font.BOLD, 26f));
        pageTitle.setForeground(Ui.TEXT);
        JLabel welcome = Ui.muted("Signed in as " + username);
        header.add(pageTitle, BorderLayout.WEST);
        header.add(welcome, BorderLayout.EAST);
        body.add(header, BorderLayout.NORTH);
        body.add(content, BorderLayout.CENTER);
        return body;
    }

    private void showPage(String key, String title) {
        pageTitle.setText(title);
        switch (key) {
            case "dashboard" -> dashboard.refresh();
            case "members" -> members.refresh();
            case "memberships" -> memberships.refresh();
            case "payments" -> payments.refresh();
            default -> {
            }
        }
        cards.show(content, key);
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            boolean active = entry.getKey().equals(key);
            if (entry.getValue() instanceof FlatButton flat) {
                flat.setFill(active ? Ui.SIDEBAR_ACTIVE : new Color(0x184852));
            }
        }
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Log out of MessMate?",
                "Logout",
                JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
