package com.cdac.enterprise.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.cdac.enterprise.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${app.email.from:no-reply@cdac-enterprise.local}")
    private String fromAddress;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetToken, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject("CDAC Enterprise - Password Reset Request");

            String text = String.format(
                    "Hello %s,\n\n" +
                    "We received a request to reset your password for your CDAC Enterprise account.\n\n" +
                    "Your password reset token is:\n%s\n\n" +
                    "To reset your password, please enter this token along with your new password.\n" +
                    "This token will expire in 1 hour.\n\n" +
                    "If you did not request a password reset, please ignore this email.\n\n" +
                    "Best regards,\n" +
                    "CDAC Enterprise Team",
                    firstName != null ? firstName : "User",
                    resetToken
            );

            message.setText(text);

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}. Error: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send password reset email. Please check the SMTP configuration.", e);
        }
    }
}
