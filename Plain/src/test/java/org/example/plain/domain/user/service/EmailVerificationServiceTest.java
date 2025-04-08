package org.example.plain.domain.user.service;

import org.example.plain.domain.user.entity.EmailVerification;
import org.example.plain.domain.user.repository.EmailVerificationRepository;
import org.example.plain.domain.user.interfaces.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_VERIFICATION_CODE = "123456";
    private static final int VERIFICATION_CODE_LENGTH = 6;
    private static final int VERIFICATION_CODE_EXPIRY_MINUTES = 5;

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    @Test
    @DisplayName("이메일 인증 코드 생성 및 전송 성공")
    void sendVerificationCode_Success() {
        // given
        EmailVerification savedVerification = EmailVerification.builder()
                .email(TEST_EMAIL)
                .code(TEST_VERIFICATION_CODE)
                .expiryDate(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES))
                .build();

        when(emailVerificationRepository.save(any(EmailVerification.class)))
                .thenReturn(savedVerification);

        // when
        String result = emailVerificationService.sendVerificationCode(TEST_EMAIL);

        // then
        assertThat(result).isNotNull();
        verify(emailVerificationRepository).save(any(EmailVerification.class));
        verify(emailService).sendVerificationEmail(TEST_EMAIL, result);
    }

    @Test
    @DisplayName("이메일 인증 코드 검증 성공")
    void verifyCode_Success() {
        // given
        EmailVerification verification = EmailVerification.builder()
                .email(TEST_EMAIL)
                .code(TEST_VERIFICATION_CODE)
                .expiryDate(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES))
                .build();

        when(emailVerificationRepository.findById(TEST_EMAIL))
                .thenReturn(Optional.of(verification));

        // when
        boolean result = emailVerificationService.verifyCode(TEST_EMAIL, TEST_VERIFICATION_CODE);

        // then
        assertThat(result).isTrue();
        verify(emailVerificationRepository).findById(TEST_EMAIL);
        verify(emailVerificationRepository).delete(verification);
    }

    @Test
    @DisplayName("이메일 인증 코드 검증 실패 - 잘못된 코드")
    void verifyCode_Fail_InvalidCode() {
        // given
        EmailVerification verification = EmailVerification.builder()
                .email(TEST_EMAIL)
                .code(TEST_VERIFICATION_CODE)
                .expiryDate(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES))
                .build();

        when(emailVerificationRepository.findById(TEST_EMAIL))
                .thenReturn(Optional.of(verification));

        // when & then
        assertThatThrownBy(() -> emailVerificationService.verifyCode(TEST_EMAIL, "wrong_code"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잘못된 인증 코드입니다");

        verify(emailVerificationRepository).findById(TEST_EMAIL);
        verify(emailVerificationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("이메일 인증 코드 검증 실패 - 만료된 코드")
    void verifyCode_Fail_ExpiredCode() {
        // given
        EmailVerification verification = EmailVerification.builder()
                .email(TEST_EMAIL)
                .code(TEST_VERIFICATION_CODE)
                .expiryDate(LocalDateTime.now().minusMinutes(1))
                .build();

        when(emailVerificationRepository.findById(TEST_EMAIL))
                .thenReturn(Optional.of(verification));

        // when & then
        assertThatThrownBy(() -> emailVerificationService.verifyCode(TEST_EMAIL, TEST_VERIFICATION_CODE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("인증 코드가 만료되었습니다");

        verify(emailVerificationRepository).findById(TEST_EMAIL);
        verify(emailVerificationRepository).delete(verification);
    }

    @Test
    @DisplayName("이메일 인증 코드 검증 실패 - 존재하지 않는 이메일")
    void verifyCode_Fail_EmailNotFound() {
        // given
        when(emailVerificationRepository.findById(TEST_EMAIL))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> emailVerificationService.verifyCode(TEST_EMAIL, TEST_VERIFICATION_CODE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잘못된 인증 코드입니다");

        verify(emailVerificationRepository).findById(TEST_EMAIL);
        verify(emailVerificationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("인증 코드 생성 테스트")
    void generateVerificationCode_ValidFormat() {
        // when
        String code = emailVerificationService.sendVerificationCode(TEST_EMAIL);

        // then
        assertThat(code).hasSize(VERIFICATION_CODE_LENGTH)
                .matches("\\d+");
    }
} 