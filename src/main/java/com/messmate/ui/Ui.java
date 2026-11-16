package com.messmate.ui;

import com.messmate.model.Member;
import com.messmate.util.AppException;
import com.messmate.util.DateRanges;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public final class Ui {
    public static final Color SIDEBAR = new Color(0x123C45);
    public static final Color SIDEBAR_ACTIVE = new Color(0x1F6A78);
    public static final Color ACCENT = new Color(0xE0B15A);
    public static final Color BG = new Color(0xF4F7F8);
    public static final Color TEXT = new Color(0x1E2A32);
    public static final Color MUTED = new Color(0x5C6B73);
    public static final Color PRIMARY = new Color(0x0E7C66);
    public static final Color DANGER = new Color(0xC0392B);
    public static final Color LINE = new Color(0xE1E6EA);
    public static final Font FONT = new Font("Segoe UI", Font.PLAIN, 14);

    private Ui() {
    }

    public static void installLook() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // Keep the platform look and feel when Nimbus is unavailable.
        }
        UIManager.put("Label.font", FONT);
        UIManager.put("Button.font", FONT);
        UIManager.put("TextField.font", FONT);
        UIManager.put("TextArea.font", FONT);
        UIManager.put("ComboBox.font", FONT);
        UIManager.put("Table.font", FONT);
        UIManager.put("TableHeader.font", FONT.deriveFont(Font.BOLD));
        UIManager.put("OptionPane.messageFont", FONT);
        UIManager.put("OptionPane.buttonFont", FONT);
    }

    public static JPanel page() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setOpaque(true);
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(8, 20, 20, 20));
        return panel;
    }

    public static JPanel card() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    public static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel muted(String text) {
        JLabel label = label(text);
        label.setForeground(MUTED);
        label.setFont(FONT.deriveFont(12f));
        return label;
    }

    public static JTextField textField() {
        JTextField field = new JTextField();
        field.setFont(FONT);
        field.setMargin(new Insets(6, 8, 6, 8));
        return field;
    }

    public static JTextArea textArea() {
        JTextArea area = new JTextArea(3, 20);
        area.setFont(FONT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setMargin(new Insets(6, 8, 6, 8));
        return area;
    }

    public static JPanel field(String caption, JComponent input) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.add(label(caption), BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        return panel;
    }

    public static JButton primary(String text) {
        return new FlatButton(text, PRIMARY, Color.WHITE);
    }

    public static JButton danger(String text) {
        return new FlatButton(text, DANGER, Color.WHITE);
    }

    public static JButton secondary(String text) {
        return new FlatButton(text, new Color(0xE6EBEE), TEXT);
    }

    public static JButton navButton(String text) {
        FlatButton button = new FlatButton(text, SIDEBAR, Color.WHITE);
        button.setHorizontalAlignment(JButton.LEFT);
        button.setFont(FONT.deriveFont(Font.BOLD, 14f));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        button.setPreferredSize(new Dimension(200, 46));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static void markNav(JButton button, boolean active) {
        button.setBackground(active ? SIDEBAR_ACTIVE : SIDEBAR);
        if (button instanceof FlatButton) {
            button.repaint();
        }
    }

    public static JSpinner dateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        spinner.setFont(FONT);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MM-dd"));
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
        editor.getTextField().setFont(FONT);
        return spinner;
    }

    public static LocalDate readDate(JSpinner spinner, String label) {
        try {
            spinner.commitEdit();
        } catch (ParseException ex) {
            throw new AppException(label + " must use yyyy-MM-dd.");
        }
        Object value = spinner.getValue();
        if (!(value instanceof Date date)) {
            throw new AppException(label + " is required.");
        }
        return DateRanges.toLocalDate(date);
    }

    public static JComboBox<String> monthCombo() {
        JComboBox<String> combo = new JComboBox<>();
        combo.setFont(FONT);
        YearMonth start = YearMonth.now().plusMonths(1);
        for (int i = 0; i <= 13; i++) {
            combo.addItem(start.minusMonths(i).toString());
        }
        combo.setSelectedItem(YearMonth.now().toString());
        return combo;
    }

    public static JComboBox<Member> memberCombo() {
        JComboBox<Member> combo = new JComboBox<>();
        combo.setFont(FONT);
        combo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel();
            label.setOpaque(true);
            label.setFont(FONT);
            label.setBorder(new EmptyBorder(4, 8, 4, 8));
            if (value instanceof Member member) {
                label.setText(member.getName() + "   |   " + member.getPhone());
            } else {
                label.setText(" ");
            }
            label.setBackground(isSelected ? new Color(0xD9F0EA) : Color.WHITE);
            label.setForeground(TEXT);
            return label;
        });
        return combo;
    }

    public static void fillMembers(JComboBox<Member> combo, List<Member> members) {
        Object previous = combo.getSelectedItem();
        Integer previousId = previous instanceof Member member ? member.getId() : null;
        combo.removeAllItems();
        Member keep = null;
        for (Member member : members) {
            combo.addItem(member);
            if (previousId != null && previousId.equals(member.getId())) {
                keep = member;
            }
        }
        if (keep != null) {
            combo.setSelectedItem(keep);
        } else if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT);
        table.setRowHeight(32);
        table.setFillsViewportHeight(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(0xE6EBEE));
        table.setSelectionBackground(new Color(0xD9F0EA));
        table.setSelectionForeground(TEXT);
        table.setBackground(Color.WHITE);
        table.setAutoCreateRowSorter(true);
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(source, value, false, false, row, column);
                label.setBackground(SIDEBAR);
                label.setForeground(Color.WHITE);
                label.setFont(FONT.deriveFont(Font.BOLD));
                label.setBorder(new EmptyBorder(8, 8, 8, 8));
                label.setOpaque(true);
                return label;
            }
        });
    }

    public static DefaultTableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String text = value == null ? "" : value.toString();
                label.setText(text);
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setOpaque(true);
                if (!isSelected) {
                    switch (text) {
                        case "Active", "Paid" -> {
                            label.setBackground(new Color(0xE5F6EE));
                            label.setForeground(new Color(0x0E7C66));
                        }
                        case "Expired" -> {
                            label.setBackground(new Color(0xFDEDEC));
                            label.setForeground(DANGER);
                        }
                        case "Upcoming", "Pending" -> {
                            label.setBackground(new Color(0xFEF5E7));
                            label.setForeground(new Color(0xB9770E));
                        }
                        default -> {
                            label.setBackground(Color.WHITE);
                            label.setForeground(TEXT);
                        }
                    }
                }
                return label;
            }
        };
    }

    public static JScrollPane scroll(JTable table) {
        styleTable(table);
        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(BorderFactory.createLineBorder(LINE));
        pane.getViewport().setBackground(Color.WHITE);
        return pane;
    }

    public static GridBagConstraints gbc(int x, int y, int width, double weightx) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = width;
        constraints.weightx = weightx;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(4, 6, 4, 6);
        constraints.anchor = GridBagConstraints.NORTHWEST;
        return constraints;
    }

    public static void error(Component parent, Exception ex) {
        String message = ex.getMessage() == null ? "Something went wrong." : ex.getMessage();
        if (!(ex instanceof AppException)) {
            ex.printStackTrace();
            message = "Unexpected error: " + message;
        }
        JOptionPane.showMessageDialog(parent, message, "MessMate", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "MessMate", JOptionPane.INFORMATION_MESSAGE);
    }
}
