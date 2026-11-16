package com.messmate.ui;

import com.messmate.model.DashboardSummary;
import com.messmate.model.Payment;
import com.messmate.service.DashboardService;
import com.messmate.util.AppException;
import com.messmate.util.Money;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

public class DashboardPanel extends JPanel {
    private final DashboardService dashboardService = new DashboardService();
    private final StatCard members = new StatCard("Total members", Ui.SIDEBAR);
    private final StatCard active = new StatCard("Active memberships", Ui.PRIMARY);
    private final StatCard expired = new StatCard("Expired memberships", Ui.DANGER);
    private final StatCard collected = new StatCard("Payments collected", Ui.ACCENT);
    private final PaymentTableModel model = new PaymentTableModel();
    private final JLabel empty = Ui.muted("No payments recorded yet.");

    public DashboardPanel() {
        super(new BorderLayout(0, 16));
        setOpaque(false);
        JPanel page = Ui.page();

        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 14));
        cards.setOpaque(false);
        cards.add(members);
        cards.add(active);
        cards.add(expired);
        cards.add(collected);

        JTable table = new JTable(model);
        JScrollPane scroll = Ui.scroll(table);
        JPanel card = Ui.card();
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JLabel heading = Ui.label("Recent payments");
        heading.setFont(Ui.FONT.deriveFont(java.awt.Font.BOLD, 16f));
        FlatButton refresh = new FlatButton("Refresh", Ui.PRIMARY, Color.WHITE);
        refresh.addActionListener(event -> refresh());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        actions.add(refresh);
        titleRow.add(heading, BorderLayout.WEST);
        titleRow.add(actions, BorderLayout.EAST);
        card.add(titleRow, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(empty, BorderLayout.SOUTH);

        page.add(cards, BorderLayout.NORTH);
        page.add(card, BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);
    }

    public void refresh() {
        try {
            DashboardSummary summary = dashboardService.load();
            members.setValue(Integer.toString(summary.totalMembers()));
            active.setValue(Integer.toString(summary.activeMemberships()));
            expired.setValue(Integer.toString(summary.expiredMemberships()));
            collected.setValue(Money.format(summary.totalCollected()));
            model.setRows(summary.recentPayments());
            empty.setText(summary.recentPayments().isEmpty() ? "No payments recorded yet." : " ");
        } catch (AppException ex) {
            Ui.error(this, ex);
        }
    }

    private static final class PaymentTableModel extends AbstractTableModel {
        private final String[] columns = {"Receipt", "Member", "Amount", "Date", "Month", "Status"};
        private List<Payment> rows = new ArrayList<>();

        void setRows(List<Payment> rows) {
            this.rows = new ArrayList<>(rows);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Payment payment = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> payment.getReceiptNumber();
                case 1 -> payment.getMemberName();
                case 2 -> Money.format(payment.getAmount());
                case 3 -> payment.getPaymentDate().toString();
                case 4 -> payment.getPaymentMonth();
                case 5 -> payment.getStatus();
                default -> "";
            };
        }
    }
}
