package org.example.plain.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.plain.domain.user.entity.EmailVerification;
import org.example.plain.domain.user.repository.EmailVerificationRepository;
import org.example.plain.domain.user.interfaces.EmailService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailService emailService;
    private final int verificationCodeLength = 6;
    private final int verificationCodeExpiryMinutes = 5;

    public String sendVerificationCode(String email) {
        String code = generateVerificationCode();
        
        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .code(code)
                .expiryDate(LocalDateTime.now().plusMinutes(verificationCodeExpiryMinutes))
                .build();

        emailVerificationRepository.save(verification);
        emailService.sendVerificationEmail(email, code);
        
        return code;
    }

    public boolean verifyCode(String email, String code) {
        EmailVerification verification = emailVerificationRepository.findById(email)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 인증 코드입니다"));

        if (!verification.getCode().equals(code)) {
            throw new IllegalArgumentException("잘못된 인증 코드입니다");
        }

        if (verification.getExpiryDate().isBefore(LocalDateTime.now())) {
            emailVerificationRepository.delete(verification);
            throw new IllegalArgumentException("인증 코드가 만료되었습니다");
        }

        verification.setVerified(true);
        emailVerificationRepository.save(verification);
        return true;
    }

    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < verificationCodeLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
} 