package com.swd392.group1.pes.services;

public interface MailService {
    void sendMail(String to, String subject, String heading, String body);
}
