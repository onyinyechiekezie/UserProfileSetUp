package com.userprofilesetup.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @MockBean
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.backend.verification-url:http://localhost:8080/api/v1/auth/verify}")
    private String verificationUrl;

    @BeforeEach
    void setup() {
        reset(mailSender);
    }

    @Test
    void sendVerificationEmailShouldSendProperMail() {
        String toEmail = "testuser@example.com";
        String token = "test-token-123";
        emailService.sendVerificationEmail(toEmail, token);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();

        assertThat(sentMessage.getFrom()).isEqualTo(fromAddress);
        assertThat(sentMessage.getTo()).containsExactly(toEmail);
        assertThat(sentMessage.getSubject()).isEqualTo("Verify Your Email Address");

        assertThat(sentMessage.getText()).contains("Please verify your email")
                .contains("token=" + token)
                .contains(verificationUrl);
    }

    @Test
    void shouldNotThrowErrorWhenSendingEmail() {
        String toEmail = "validuser@example.com";
        String token = "safe-token";
        emailService.sendVerificationEmail(toEmail, token);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldBuildVerificationLinkCorrectly() {
        String toEmail = "user@example.com";
        String token = "abcd-1234";

        emailService.sendVerificationEmail(toEmail, token);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        String text = captor.getValue().getText();

        assertThat(text).contains(verificationUrl + "?token=" + token);
    }
}
