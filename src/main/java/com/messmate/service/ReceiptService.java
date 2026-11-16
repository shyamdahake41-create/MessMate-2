package com.messmate.service;

import com.messmate.model.Payment;
import com.messmate.util.AppException;
import com.messmate.util.Money;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.nio.file.Path;

public final class ReceiptService {
    private ReceiptService() {
    }

    public static String toText(Payment payment) {
        String line = "-".repeat(44);
        return """
                MESSMATE
                Mess Membership Receipt
                %s
                Receipt No    : %s
                Member        : %s
                Phone         : %s
                Amount        : %s
                Payment Date  : %s
                Payment Month : %s
                Status        : %s
                %s
                This is a computer-generated receipt.
                """.formatted(
                line,
                payment.getReceiptNumber(),
                payment.getMemberName(),
                payment.getMemberPhone(),
                Money.format(payment.getAmount()),
                payment.getPaymentDate(),
                payment.getPaymentMonth(),
                payment.getStatus(),
                line
        );
    }

    public static void writePdf(Payment payment, Path file) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            float width = page.getMediaBox().getWidth();
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.setNonStrokingColor(0.07f, 0.24f, 0.27f);
                stream.addRect(40, 700, width - 80, 90);
                stream.fill();
                stream.setNonStrokingColor(1f, 1f, 1f);
                text(stream, bold, 22, 60, 752, "MessMate");
                stream.setNonStrokingColor(0.91f, 0.78f, 0.45f);
                text(stream, regular, 12, 60, 724, "Mess Membership Receipt");

                stream.setStrokingColor(0.07f, 0.24f, 0.27f);
                stream.setLineWidth(1.2f);
                stream.addRect(40, 160, width - 80, 530);
                stream.stroke();

                stream.setNonStrokingColor(0.12f, 0.16f, 0.20f);
                float y = 650;
                y = row(stream, bold, regular, y, "Receipt No", payment.getReceiptNumber());
                y = row(stream, bold, regular, y, "Member", payment.getMemberName());
                y = row(stream, bold, regular, y, "Phone", payment.getMemberPhone());
                y = row(stream, bold, regular, y, "Amount", Money.format(payment.getAmount()));
                y = row(stream, bold, regular, y, "Payment Date", payment.getPaymentDate().toString());
                y = row(stream, bold, regular, y, "Payment Month", payment.getPaymentMonth());
                row(stream, bold, regular, y, "Status", payment.getStatus());
                text(stream, regular, 11, 60, 200, "This is a computer-generated receipt.");
            }
            document.save(file.toFile());
        } catch (IOException ex) {
            throw new AppException("Could not save the PDF receipt. " + ex.getMessage());
        }
    }

    public static void print(Payment payment) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("MessMate " + payment.getReceiptNumber());
        job.setPrintable((Graphics graphics, PageFormat pageFormat, int pageIndex) -> {
            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }
            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            g2.setColor(Color.BLACK);
            g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            int y = 40;
            for (String line : toText(payment).split("\\R")) {
                g2.drawString(line, 20, y);
                y += 18;
            }
            return Printable.PAGE_EXISTS;
        });
        if (!job.printDialog()) {
            return;
        }
        try {
            job.print();
        } catch (PrinterException ex) {
            throw new AppException("Could not print the receipt.");
        }
    }

    private static float row(PDPageContentStream stream, PDType1Font labelFont, PDType1Font valueFont,
                             float y, String label, String value) throws IOException {
        text(stream, labelFont, 12, 60, y, label);
        text(stream, valueFont, 12, 220, y, pdfSafe(value));
        return y - 36;
    }

    private static void text(PDPageContentStream stream, PDType1Font font, float size, float x, float y, String value)
            throws IOException {
        stream.beginText();
        stream.setFont(font, size);
        stream.newLineAtOffset(x, y);
        stream.showText(pdfSafe(value));
        stream.endText();
    }

    static String pdfSafe(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            out.append(c >= 32 && c <= 126 ? c : ' ');
        }
        return out.toString();
    }
}
