package com.messmate.ui;

import com.messmate.model.Payment;
import com.messmate.service.ReceiptService;
import com.messmate.util.AppException;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.io.File;
import java.nio.file.Path;
import java.util.Locale;

public class ReceiptDialog extends JDialog {
    public ReceiptDialog(Window owner, Payment payment) {
        super(owner, "Receipt " + payment.getReceiptNumber(), ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JTextArea area = new JTextArea(ReceiptService.toText(payment));
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        area.setBackground(Color.WHITE);
        area.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));

        FlatButton pdf = new FlatButton("Save PDF", Ui.PRIMARY, Color.WHITE);
        FlatButton print = new FlatButton("Print", new Color(0x1F6A78), Color.WHITE);
        FlatButton close = new FlatButton("Close", new Color(0xE6EBEE), Ui.TEXT);
        pdf.addActionListener(event -> savePdf(payment));
        print.addActionListener(event -> {
            try {
                ReceiptService.print(payment);
            } catch (AppException ex) {
                Ui.error(this, ex);
            }
        });
        close.addActionListener(event -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(Ui.BG);
        buttons.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 12, 12, 12));
        buttons.add(pdf);
        buttons.add(print);
        buttons.add(close);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Ui.BG);
        root.add(new JScrollPane(area), BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
        setSize(520, 460);
        setLocationRelativeTo(owner);
    }

    private void savePdf(Payment payment) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save receipt");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF files", "pdf"));
        chooser.setSelectedFile(new File(payment.getReceiptNumber() + ".pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            file = new File(file.getParentFile(), file.getName() + ".pdf");
        }
        try {
            Path path = file.toPath();
            ReceiptService.writePdf(payment, path);
            Ui.info(this, "Receipt saved:\n" + path.toAbsolutePath());
        } catch (AppException ex) {
            Ui.error(this, ex);
        }
    }
}
