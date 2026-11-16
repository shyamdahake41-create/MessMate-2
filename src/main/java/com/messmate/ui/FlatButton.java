package com.messmate.ui;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class FlatButton extends JButton {
    private Color background;
    private Color hover;

    public FlatButton(String text, Color background, Color foreground) {
        super(text);
        setFill(background);
        setForeground(foreground);
        setFont(Ui.FONT);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(10, 16, 10, 16));
    }

    public void setFill(Color background) {
        this.background = background;
        this.hover = background.brighter();
        setBackground(background);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fill = background;
        if (!isEnabled()) {
            fill = new Color(0xC5CED4);
        } else if (getModel().isPressed()) {
            fill = background.darker();
        } else if (getModel().isRollover()) {
            fill = hover;
        }
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.dispose();
        super.paintComponent(graphics);
    }
}
