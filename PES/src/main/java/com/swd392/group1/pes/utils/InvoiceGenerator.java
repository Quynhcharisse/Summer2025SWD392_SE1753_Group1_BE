package com.swd392.group1.pes.utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InvoiceGenerator {
    public static byte[] generateInvoicePdf(String parentName, String studentName, String txnRef, long amount, LocalDateTime paymentDate) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            // Header
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph header = new Paragraph("Tuition Payment Invoice", headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);
            document.add(new Paragraph(" "));

            // Body
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            // Add rows
            table.addCell("Parent Name:");
            table.addCell(parentName);

            table.addCell("Student Name:");
            table.addCell(studentName);

            table.addCell("Transaction Ref:");
            table.addCell(txnRef);

            table.addCell("Amount Paid:");
            table.addCell(String.format("%,d VND", amount));

            table.addCell("Payment Date:");
            table.addCell(paymentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            document.add(table);

            document.add(new Paragraph("\nThank you for your payment!", new Font(Font.FontFamily.HELVETICA, 12)));

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF invoice", e);
        }

        return out.toByteArray();
    }
}
