package com.userprofilesetup.services;

public interface EmailService {
//    void sendVerificationEmail(String to, String verificationToken);
    void sendVerificationEmail(String toEmail, String token);
}
