package com.swd392.group1.pes.services;

public interface MailService {
    void sendMail(String to, String subject, String heading, String body);
    void sendInvoiceEmail(String to, String subject, String htmlBody, byte[] pdfAttachment, String filename);
}
