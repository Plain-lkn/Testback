package org.example.plain.domain.user.interfaces;

public interface EmailService {
    void sendVerificationEmail(String to, String code);
    void sendEmail(String email, String subject, String content);
    public void sendHtmlEmail(String email, String subject, String htmlContent);
} 