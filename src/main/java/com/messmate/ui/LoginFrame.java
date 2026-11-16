package com.messmate.ui;

import com.messmate.service.AuthService;
import com.messmate.util.AppException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;

public class LoginFrame extends JFrame {
    private final AuthService authService = new AuthService();
    private final JTextField username = Ui.textField();
    private final JPasswordField password = new JPasswordField();
    private final JLabel error = new JLabel(" ");

    public LoginFrame() {
        super("MessMate - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(520, 620));
        setSize(520, 640);
        password.setFont(Ui.FONT);

        JPanel background = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setPaint(new GradientPaint(0, 0, new Color(0x0F3D3E), 0, getHeight(), new Color(0x1B6B63)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };

        JPanel card = Ui.card();
        card.setLayout(new BorderLayout(0, 12));
        card.setPreferredSize(new Dimension(380, 460));

        JLabel title = new JLabel("MessMate");
        title.setFont(Ui.FONT.deriveFont(Font.BOLD, 32f));
        title.setForeground(Ui.SIDEBAR);
        JLabel subtitle = Ui.muted("Mess Membership Management");
        error.setForeground(Ui.DANGER);
        error.setFont(Ui.FONT);

        JPanel heading = new JPanel(new BorderLayout(0, 4));
        heading.setOpaque(false);
        heading.add(title, BorderLayout.NORTH);
        heading.add(subtitle, BorderLayout.CENTER);

        JPanel form = new JPanel(new BorderLayout(0, 10));
        form.setOpaque(false);
        form.add(Ui.field("Username", username), BorderLayout.NORTH);
        JPanel lower = new JPanel(new BorderLayout(0, 8));
        lower.setOpaque(false);
        lower.add(Ui.field("Password", password), BorderLayout.NORTH);
        lower.add(error, BorderLayout.CENTER);
        form.add(lower, BorderLayout.CENTER);

        FlatButton login = new FlatButton("Login", Ui.PRIMARY, Color.WHITE);
        login.addActionListener(event -> attempt());
        username.addActionListener(event -> password.requestFocusInWindow());
        password.addActionListener(event -> attempt());

        JLabel hint = Ui.muted("College demo account: admin / admin123");
        JPanel south = new JPanel(new BorderLayout(0, 10));
        south.setOpaque(false);
        south.add(login, BorderLayout.NORTH);
        south.add(hint, BorderLayout.SOUTH);

        card.add(heading, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(south, BorderLayout.SOUTH);
        background.add(card);
        setContentPane(background);
        getRootPane().setDefaultButton(login);
        setLocationRelativeTo(null);
    }

    private void attempt() {
        String user = username.getText().trim();
        String pass = new String(password.getPassword());
        if (user.isBlank() || pass.isBlank()) {
            error.setText("Enter both username and password.");
            return;
        }
        try {
            if (!authService.login(user, pass)) {
                error.setText("Invalid username or password.");
                password.setText("");
                return;
            }
            new MainFrame(user).setVisible(true);
            dispose();
        } catch (AppException ex) {
            error.setText(ex.getMessage());
        }
    }
}
