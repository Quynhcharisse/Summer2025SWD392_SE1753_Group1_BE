package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.services.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor

public class MailServiceImpl implements MailService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    /**
     * @param to địa chỉ nhận
     * @param subject tiêu đề email (<title> và mailbox)
     * @param heading tiêu đề hiển thị trong <h2>
     * @param body HTML fragment để chèn vào <div th:utext="${body}">…</div>
     */

    @Async
    @Override
    public void sendMail(String to,
                         String subject,
                         String heading,
                         String body) {
        try {
            // 1) Tạo MimeMessage
            MimeMessage msg = javaMailSender.createMimeMessage();
            // true = multipart
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

            // 2) Chuẩn bị Thymeleaf context và render HTML
            Context context = new Context();
            context.setVariable("title", subject);
            context.setVariable("heading", heading);
            context.setVariable("body", body);
            String html = templateEngine.process("email/base", context);

            // 3) Set to, subject, html content
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            // 4) Đính kèm logo inline (CID = "logo")
            ClassPathResource logo = new ClassPathResource("static/img/logo.png");
            helper.addInline("logo", logo);

            // 5) Gửi mail
            javaMailSender.send(msg);

        } catch (MessagingException ex) {
            throw new IllegalStateException("Failed to send email", ex);
        }
    }
}
