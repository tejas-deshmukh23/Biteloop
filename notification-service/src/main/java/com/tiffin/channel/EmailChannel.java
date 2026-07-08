package com.tiffin.channel;

import com.tiffin.entity.NotificationEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Email notification channel via Gmail SMTP.
 *
 * Uses Spring's JavaMailSender — configured in application.yml
 * with Gmail SMTP settings and App Password.
 *
 * Why SimpleMailMessage and not MimeMessage?
 * SimpleMailMessage is plain text — sufficient for MVP.
 * When we add Thymeleaf HTML templates in Phase 2,
 * we switch to MimeMessage here only — nothing else changes.
 */
@Component
public class EmailChannel implements NotificationChannel {

    private static final Logger log =
            LoggerFactory.getLogger(EmailChannel.class);

    private final JavaMailSender mailSender;

    @Value("${notification.from-email}")
    private String fromEmail;

    public EmailChannel(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public boolean send(String recipient, String subject,
                        String body, NotificationEventType eventType) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(recipient);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            log.info("Email sent successfully: to={} subject={} eventType={}",
                    recipient, subject, eventType);
            return true;

        } catch (MailException e) {
            log.error("Email failed: to={} eventType={} reason={}",
                    recipient, eventType, e.getMessage());
            return false;
        }
    }

    @Override
    public com.tiffin.entity.NotificationChannel getChannelType() {
        return com.tiffin.entity.NotificationChannel.EMAIL;
    }
}