package com.messmate.ui;

import com.messmate.model.Member;
import com.messmate.service.MemberService;
import com.messmate.util.AppException;
import com.messmate.util.DateRanges;
import com.messmate.util.Money;
import com.messmate.util.Validators;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MembersPanel extends JPanel {
    private final MemberService memberService = new MemberService();
    private final MemberTableModel model = new MemberTableModel();
    private final JTable table = new JTable(model);
    private final JTextField search = Ui.textField();
    private final JTextField name = Ui.textField();
    private final JTextField phone = Ui.textField();
    private final JTextArea address = Ui.textArea();
    private final JSpinner joiningDate = Ui.dateSpinner();
    private final JTextField fee = Ui.textField();
    private final JLabel status = Ui.muted("Enter details to add a member, or select a row to edit.");
    private final FlatButton saveButton = new FlatButton("Add Member", Ui.PRIMARY, java.awt.Color.WHITE);
    private Integer editingId;
    private boolean updatingTable;

    public MembersPanel() {
        super(new BorderLayout());
        setOpaque(false);
        JPanel page = Ui.page();

        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);
        top.add(Ui.field("Search by name or phone", search), BorderLayout.CENTER);
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                reload();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                reload();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                reload();
            }
        });

        table.getSelectionModel().addListSelectionListener(event -> {
            if (event.getValueIsAdjusting() || updatingTable) {
                return;
            }
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                return;
            }
            loadForm(model.row(table.convertRowIndexToModel(viewRow)));
        });

        JPanel form = Ui.card();
        form.setLayout(new GridBagLayout());
        JScrollPane addressScroll = new JScrollPane(address);
        form.add(Ui.field("Name", name), Ui.gbc(0, 0, 1, 1));
        form.add(Ui.field("Phone number", phone), Ui.gbc(1, 0, 1, 1));
        form.add(Ui.field("Address", addressScroll), Ui.gbc(0, 1, 2, 1));
        form.add(Ui.field("Joining date", joiningDate), Ui.gbc(0, 2, 1, 1));
        form.add(Ui.field("Monthly fee", fee), Ui.gbc(1, 2, 1, 1));

        saveButton.addActionListener(event -> save());
        FlatButton clear = new FlatButton("Clear", new java.awt.Color(0xE6EBEE), Ui.TEXT);
        clear.addActionListener(event -> clearForm());
        FlatButton delete = new FlatButton("Delete", Ui.DANGER, java.awt.Color.WHITE);
        delete.addActionListener(event -> deleteSelected());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(saveButton);
        buttons.add(clear);
        buttons.add(delete);
        form.add(buttons, Ui.gbc(0, 3, 2, 1));
        form.add(status, Ui.gbc(0, 4, 2, 1));

        page.add(top, BorderLayout.NORTH);
        page.add(Ui.scroll(table), BorderLayout.CENTER);
        page.add(form, BorderLayout.SOUTH);
        add(page, BorderLayout.CENTER);
    }

    public void refresh() {
        reload();
    }

    private void reload() {
        try {
            updatingTable = true;
            model.setRows(memberService.search(search.getText()));
        } catch (AppException ex) {
            Ui.error(this, ex);
        } finally {
            updatingTable = false;
        }
    }

    private void loadForm(Member member) {
        editingId = member.getId();
        name.setText(member.getName());
        phone.setText(member.getPhone());
        address.setText(member.getAddress());
        joiningDate.setValue(DateRanges.toDate(member.getJoiningDate()));
        fee.setText(String.format(Locale.US, "%.2f", member.getMonthlyFee()));
        saveButton.setText("Update Member");
        status.setText("Editing " + member.getName() + ". Save to update, or Clear to add someone new.");
    }

    private void clearForm() {
        editingId = null;
        name.setText("");
        phone.setText("");
        address.setText("");
        joiningDate.setValue(new Date());
        fee.setText("");
        saveButton.setText("Add Member");
        table.clearSelection();
        status.setText("Enter details to add a member, or select a row to edit.");
    }

    private void save() {
        try {
            Member member = readForm();
            boolean updating = editingId != null;
            if (updating) {
                member.setId(editingId);
                memberService.update(member);
            } else {
                memberService.add(member);
            }
            clearForm();
            reload();
            status.setText(updating ? "Member updated." : "Member added.");
        } catch (AppException ex) {
            Ui.error(this, ex);
        }
    }

    private Member readForm() {
        Member member = new Member();
        member.setName(name.getText());
        member.setPhone(phone.getText());
        member.setAddress(address.getText());
        member.setJoiningDate(Ui.readDate(joiningDate, "Joining date"));
        member.setMonthlyFee(Validators.parseMoney("Monthly fee", fee.getText()));
        return member;
    }

    private void deleteSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            Ui.error(this, new AppException("Select a member in the table to delete."));
            return;
        }
        Member member = model.row(table.convertRowIndexToModel(viewRow));
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete " + member.getName() + "? Memberships and payments for this member will also be removed.",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            memberService.delete(member.getId());
            clearForm();
            reload();
            status.setText("Member deleted.");
        } catch (AppException ex) {
            Ui.error(this, ex);
        }
    }

    private static final class MemberTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Name", "Phone", "Address", "Joining Date", "Monthly Fee"};
        private List<Member> rows = new ArrayList<>();

        void setRows(List<Member> rows) {
            this.rows = new ArrayList<>(rows);
            fireTableDataChanged();
        }

        Member row(int index) {
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
            Member member = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> member.getId();
                case 1 -> member.getName();
                case 2 -> member.getPhone();
                case 3 -> member.getAddress();
                case 4 -> member.getJoiningDate().toString();
                case 5 -> Money.format(member.getMonthlyFee());
                default -> "";
            };
        }
    }
}
