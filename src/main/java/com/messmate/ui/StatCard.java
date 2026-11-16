package com.messmate.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

public class StatCard extends JPanel {
    private final JLabel valueLabel;

    public StatCard(String title, Color accent) {
        setLayout(new BorderLayout(12, 0));
        setBackground(Color.WHITE);
        setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(Ui.LINE, 1, true),
                new EmptyBorder(14, 8, 14, 14)
        ));
        JPanel stripe = new JPanel();
        stripe.setBackground(accent);
        stripe.setPreferredSize(new Dimension(6, 10));
        JLabel titleLabel = Ui.muted(title.toUpperCase());
        valueLabel = new JLabel("0");
        valueLabel.setFont(Ui.FONT.deriveFont(Font.BOLD, 26f));
        valueLabel.setForeground(Ui.TEXT);
        JPanel text = new JPanel(new BorderLayout(0, 6));
        text.setOpaque(false);
        text.add(titleLabel, BorderLayout.NORTH);
        text.add(valueLabel, BorderLayout.CENTER);
        add(stripe, BorderLayout.WEST);
        add(text, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }
}
