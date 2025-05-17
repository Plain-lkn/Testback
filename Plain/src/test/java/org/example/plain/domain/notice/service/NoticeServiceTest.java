package org.example.plain.domain.notice.service;

import org.example.plain.common.ResponseField;
import org.example.plain.common.enums.Role;
import org.example.plain.domain.classLecture.entity.ClassLecture;
import org.example.plain.domain.classLecture.repository.ClassLectureRepositoryPort;
import org.example.plain.domain.notice.dto.NoticeRequest;
import org.example.plain.domain.notice.dto.NoticeResponse;
import org.example.plain.domain.notice.dto.NoticeUpdateRequest;
import org.example.plain.domain.notice.entity.NoticeEntity;
import org.example.plain.domain.notice.repository.NoticeCommentRepository;
import org.example.plain.domain.notice.repository.NoticeRepository;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private NoticeCommentRepository noticeCommentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClassLectureRepositoryPort classLectureRepositoryPort;

    @InjectMocks
    private NoticeService noticeService;

    private User testUser;
    private ClassLecture testClassLecture;
    private NoticeEntity testNotice;
    private NoticeRequest testNoticeRequest;
    private NoticeUpdateRequest testNoticeUpdateRequest;
    private String testNoticeId;

    @BeforeEach
    void setUp() throws Exception {
        // Test User 생성
        testUser = User.builder()
                .id("test-user-id")
                .username("testuser")
                .email("test@example.com")
                .role(Role.NORMAL)
                .build();

        // Test ClassLecture 생성
        testClassLecture = ClassLecture.builder()
                .id("class-lecture-id")
                .title("Test Class")
                .description("Test Class Description")
                .code("TEST123")
                .instructor(testUser)
                .maxMember(30L)
                .build();

        // 테스트 ID 생성
        testNoticeId = UUID.randomUUID().toString();

        // NoticeEntity 생성
        testNotice = NoticeEntity.create(
                "Test Notice Title",
                "Test Notice Content",
                testUser,
                testClassLecture
        );
        // 리플렉션을 사용하여 UUID 설정
        setField(testNotice, "noticeId", testNoticeId);

        // NoticeRequest 생성 - 리플렉션 사용
        testNoticeRequest = new NoticeRequest();
        setField(testNoticeRequest, "title", "Test Notice Title");
        setField(testNoticeRequest, "content", "Test Notice Content");
        setField(testNoticeRequest, "c_id", "class-lecture-id");

        // NoticeUpdateRequest 생성 - 리플렉션 사용
        testNoticeUpdateRequest = new NoticeUpdateRequest();
        setField(testNoticeUpdateRequest, "noticeId", testNoticeId);
        setField(testNoticeUpdateRequest, "title", "Updated Title");
        setField(testNoticeUpdateRequest, "content", "Updated Content");
    }
    
    // 리플렉션을 사용하여 private 필드 설정하는 헬퍼 메서드
    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @Test
    @DisplayName("공지사항 생성 테스트")
    void createNotice() {
        // Given
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
        when(classLectureRepositoryPort.findById("class-lecture-id")).thenReturn(testClassLecture);
        when(noticeRepository.save(any(NoticeEntity.class))).thenReturn(testNotice);

        // When
        ResponseField<NoticeResponse> response = noticeService.createNotice(testNoticeRequest, "test-user-id");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        verify(noticeRepository, times(1)).save(any(NoticeEntity.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 공지사항 생성 시 예외 발생")
    void createNotice_UserNotFound() {
        // Given
        when(userRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> noticeService.createNotice(testNoticeRequest, "non-existent-id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 사용자입니다.");
    }

    @Test
    @DisplayName("공지사항 수정 테스트")
    void updateNotice() {
        // Given
        when(noticeRepository.findById(testNoticeId)).thenReturn(Optional.of(testNotice));

        // When
        ResponseField<NoticeResponse> response = noticeService.updateNotice(testNoticeUpdateRequest, "test-user-id");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("권한 없는 사용자가 공지사항 수정 시 예외 발생")
    void updateNotice_Unauthorized() {
        // Given
        when(noticeRepository.findById(testNoticeId)).thenReturn(Optional.of(testNotice));

        // When & Then
        assertThatThrownBy(() -> noticeService.updateNotice(testNoticeUpdateRequest, "another-user-id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수정 권한이 없습니다.");
    }

    @Test
    @DisplayName("모든 공지사항 조회 테스트")
    void getAllNotice() {
        // Given
        List<NoticeEntity> noticeList = Arrays.asList(testNotice);
        when(noticeRepository.findAll()).thenReturn(noticeList);

        // When
        ResponseField<List<NoticeResponse>> response = noticeService.getAllNotice();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("ID로 공지사항 조회 테스트")
    void getNotice() {
        // Given
        when(noticeRepository.findById(testNoticeId)).thenReturn(Optional.of(testNotice));

        // When
        ResponseField<NoticeResponse> response = noticeService.getNotice(testNoticeId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("공지사항 삭제 테스트")
    void deleteNotice() {
        // Given
        when(noticeRepository.findById(testNoticeId)).thenReturn(Optional.of(testNotice));
        
        // When
        noticeService.deleteNotice(testNoticeId, "test-user-id");
        
        // Then
        verify(noticeRepository, times(1)).delete(testNotice);
    }

    @Test
    @DisplayName("권한 없는 사용자가 공지사항 삭제 시 예외 발생")
    void deleteNotice_Unauthorized() {
        // Given
        when(noticeRepository.findById(testNoticeId)).thenReturn(Optional.of(testNotice));
        
        // When & Then
        assertThatThrownBy(() -> noticeService.deleteNotice(testNoticeId, "another-user-id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제 권한이 없습니다.");
    }
} 