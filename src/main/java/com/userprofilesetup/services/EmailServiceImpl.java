package com.userprofilesetup.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${app.backend.verification-url:http://localhost:8080/api/v1/auth/verify}")
    private String verificationUrl;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "Verify Your Email Address";
        String verificationLink = verificationUrl + "?token=" + token;
        String body = "Hi,\n\nPlease verify your email by clicking this link:\n" + verificationLink +
                "\n\nIf you didn’t request this, you can safely ignore this message.\n\nRegards\nEnum Talent Team.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}
