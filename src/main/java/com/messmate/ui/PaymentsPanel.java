package com.messmate.ui;

import com.messmate.model.Member;
import com.messmate.model.Payment;
import com.messmate.model.PaymentStatus;
import com.messmate.service.MemberService;
import com.messmate.service.PaymentService;
import com.messmate.util.AppException;
import com.messmate.util.Money;
import com.messmate.util.Validators;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PaymentsPanel extends JPanel {
    private final MemberService memberService = new MemberService();
    private final PaymentService paymentService = new PaymentService();
    private final JComboBox<Member> members = Ui.memberCombo();
    private final JTextField amount = Ui.textField();
    private final JSpinner paymentDate = Ui.dateSpinner();
    private final JComboBox<String> month = Ui.monthCombo();
    private final JComboBox<String> statusCombo = new JComboBox<>(new String[]{PaymentStatus.PAID, PaymentStatus.PENDING});
    private final JTextField search = Ui.textField();
    private final PaymentTableModel model = new PaymentTableModel();
    private final JTable table = new JTable(model);
    private final JLabel status = Ui.muted("One payment is allowed for each member and month.");
    private final FlatButton recordButton = new FlatButton("Record Payment", Ui.PRIMARY, Color.WHITE);

    public PaymentsPanel() {
        super(new BorderLayout());
        setOpaque(false);
        JPanel page = Ui.page();

        members.addActionListener(event -> {
            Object selected = members.getSelectedItem();
            if (selected instanceof Member member) {
                amount.setText(String.format(Locale.US, "%.2f", member.getMonthlyFee()));
            }
        });
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                reloadTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                reloadTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                reloadTable();
            }
        });

        JPanel form = Ui.card();
        form.setLayout(new GridBagLayout());
        form.add(Ui.field("Member", members), Ui.gbc(0, 0, 1, 1));
        form.add(Ui.field("Amount", amount), Ui.gbc(1, 0, 1, 1));
        form.add(Ui.field("Payment date", paymentDate), Ui.gbc(0, 1, 1, 1));
        form.add(Ui.field("Payment month", month), Ui.gbc(1, 1, 1, 1));
        form.add(Ui.field("Status", statusCombo), Ui.gbc(0, 2, 1, 1));
        form.add(Ui.field("Search history", search), Ui.gbc(1, 2, 1, 1));

        recordButton.addActionListener(event -> record());
        FlatButton view = new FlatButton("View Receipt", new Color(0x1F6A78), Color.WHITE);
        view.addActionListener(event -> viewSelected());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(recordButton);
        buttons.add(view);
        form.add(buttons, Ui.gbc(0, 3, 2, 1));
        form.add(status, Ui.gbc(0, 4, 2, 1));

        page.add(form, BorderLayout.NORTH);
        page.add(Ui.scroll(table), BorderLayout.CENTER);
        table.getColumnModel().getColumn(6).setCellRenderer(Ui.statusRenderer());
        add(page, BorderLayout.CENTER);
    }

    public void refresh() {
        try {
            Ui.fillMembers(members, memberService.search(""));
            reloadTable();
        } catch (AppException ex) {
            Ui.error(this, ex);
        }
    }

    private void reloadTable() {
        try {
            model.setRows(paymentService.search(search.getText()));
        } catch (AppException ex) {
            Ui.error(this, ex);
        }
    }

    private void record() {
        recordButton.setEnabled(false);
        try {
            Object selected = members.getSelectedItem();
            if (!(selected instanceof Member member)) {
                throw new AppException("Select a member. Add one from the Members screen if the list is empty.");
            }
            Payment payment = paymentService.record(
                    member.getId(),
                    Validators.parseMoney("Payment amount", amount.getText()),
                    Ui.readDate(paymentDate, "Payment date"),
                    (String) month.getSelectedItem(),
                    (String) statusCombo.getSelectedItem()
            );
            status.setText("Payment recorded. Receipt " + payment.getReceiptNumber() + ".");
            reloadTable();
            new ReceiptDialog(SwingUtilities.getWindowAncestor(this), payment).setVisible(true);
        } catch (AppException ex) {
            Ui.error(this, ex);
        } finally {
            recordButton.setEnabled(true);
        }
    }

    private void viewSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            Ui.error(this, new AppException("Select a payment to view its receipt."));
            return;
        }
        Payment payment = model.row(table.convertRowIndexToModel(viewRow));
        new ReceiptDialog(SwingUtilities.getWindowAncestor(this), payment).setVisible(true);
    }

    private static final class PaymentTableModel extends AbstractTableModel {
        private final String[] columns = {"Receipt", "Member", "Phone", "Amount", "Date", "Month", "Status"};
        private List<Payment> rows = new ArrayList<>();

        void setRows(List<Payment> rows) {
            this.rows = new ArrayList<>(rows);
            fireTableDataChanged();
        }

        Payment row(int index) {
            return rows.get(index);
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
                case 2 -> payment.getMemberPhone();
                case 3 -> Money.format(payment.getAmount());
                case 4 -> payment.getPaymentDate().toString();
                case 5 -> payment.getPaymentMonth();
                case 6 -> payment.getStatus();
                default -> "";
            };
        }
    }
}
