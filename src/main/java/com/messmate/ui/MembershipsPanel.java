package com.messmate.ui;

import com.messmate.model.Member;
import com.messmate.model.Membership;
import com.messmate.service.MemberService;
import com.messmate.service.MembershipService;
import com.messmate.util.AppException;
import com.messmate.util.Money;
import com.messmate.util.Validators;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MembershipsPanel extends JPanel {
    private final MemberService memberService = new MemberService();
    private final MembershipService membershipService = new MembershipService();
    private final JComboBox<Member> members = Ui.memberCombo();
    private final JSpinner startDate = Ui.dateSpinner();
    private final JTextField fee = Ui.textField();
    private final JComboBox<String> filter = new JComboBox<>(new String[]{"All", "Active", "Expired", "Upcoming"});
    private final MembershipTableModel model = new MembershipTableModel();
    private final JTable table = new JTable(model);
    private final JLabel status = Ui.muted("Assign a one-month membership, or renew the selected member.");

    public MembershipsPanel() {
        super(new BorderLayout());
        setOpaque(false);
        JPanel page = Ui.page();

        members.addActionListener(event -> {
            Object selected = members.getSelectedItem();
            if (selected instanceof Member member) {
                fee.setText(String.format(Locale.US, "%.2f", member.getMonthlyFee()));
            }
        });

        JPanel form = Ui.card();
        form.setLayout(new GridBagLayout());
        form.add(Ui.field("Member", members), Ui.gbc(0, 0, 1, 1));
        form.add(Ui.field("Start date", startDate), Ui.gbc(1, 0, 1, 1));
        form.add(Ui.field("Monthly fee", fee), Ui.gbc(0, 1, 1, 1));
        form.add(Ui.field("Show", filter), Ui.gbc(1, 1, 1, 1));

        FlatButton assign = new FlatButton("Assign Month", Ui.PRIMARY, Color.WHITE);
        assign.addActionListener(event -> assign());
        FlatButton renew = new FlatButton("Renew One Month", new Color(0x1F6A78), Color.WHITE);
        renew.addActionListener(event -> renew());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(assign);
        buttons.add(renew);
        form.add(buttons, Ui.gbc(0, 2, 2, 1));
        form.add(status, Ui.gbc(0, 3, 2, 1));
        filter.addActionListener(event -> reloadTable());

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
            String selected = (String) filter.getSelectedItem();
            model.setRows(membershipService.list(selected));
        } catch (AppException ex) {
            Ui.error(this, ex);
        }
    }

    private Member selectedMember() {
        Object value = members.getSelectedItem();
        if (!(value instanceof Member member)) {
            throw new AppException("Select a member. Add one from the Members screen if the list is empty.");
        }
        return member;
    }

    private void assign() {
        try {
            Member member = selectedMember();
            double monthlyFee = Validators.parseMoney("Monthly fee", fee.getText());
            Membership membership = membershipService.assign(member.getId(), Ui.readDate(startDate, "Start date"), monthlyFee);
            status.setText(member.getName() + " is " + membership.getStatus().toLowerCase(Locale.ROOT)
                    + " from " + membership.getStartDate() + " to " + membership.getExpiryDate() + ".");
            reloadTable();
        } catch (AppException ex) {
            Ui.error(this, ex);
        }
    }

    private void renew() {
        try {
            Member member = selectedMember();
            int choice = JOptionPane.showConfirmDialog(this,
                    "Renew " + member.getName() + " for another month?",
                    "Renew membership",
                    JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
            Membership membership = membershipService.renew(member.getId());
            status.setText("Renewed " + member.getName() + ". New period: "
                    + membership.getStartDate() + " to " + membership.getExpiryDate()
                    + " (" + membership.getStatus() + ").");
            reloadTable();
        } catch (AppException ex) {
            Ui.error(this, ex);
        }
    }

    private static final class MembershipTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Member", "Phone", "Start", "Expiry", "Fee", "Status"};
        private List<Membership> rows = new ArrayList<>();

        void setRows(List<Membership> rows) {
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
            Membership membership = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> membership.getId();
                case 1 -> membership.getMemberName();
                case 2 -> membership.getMemberPhone();
                case 3 -> membership.getStartDate().toString();
                case 4 -> membership.getExpiryDate().toString();
                case 5 -> Money.format(membership.getMonthlyFee());
                case 6 -> membership.getStatus();
                default -> "";
            };
        }
    }
}
