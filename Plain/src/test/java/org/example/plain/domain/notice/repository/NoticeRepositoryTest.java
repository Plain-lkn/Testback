package org.example.plain.domain.notice.repository;

import org.example.plain.domain.classLecture.entity.ClassLecture;
import org.example.plain.domain.classLecture.repository.ClassLectureRepositoryPort;
import org.example.plain.domain.notice.entity.NoticeEntity;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DataJpaTest
class NoticeRepositoryTest {

    @Autowired
    private NoticeRepository noticeRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ClassLectureRepositoryPort classLectureRepositoryPort;

    private User testUser;
    private ClassLecture testClassLecture;
    private NoticeEntity testNotice;

    @BeforeEach
    void setUp() {
        // Test User 생성
        testUser = User.builder()
                .id("test-user-id")
                .username("testuser")
                .email("test@example.com")
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
        
        when(classLectureRepositoryPort.save(testClassLecture)).thenReturn(testClassLecture);
        when(classLectureRepositoryPort.findById("class-lecture-id")).thenReturn(testClassLecture);

        // NoticeEntity 생성
        testNotice = NoticeEntity.create(
                "Test Notice Title",
                "Test Notice Content",
                testUser,
                testClassLecture
        );
    }

    @Test
    @DisplayName("공지사항 저장 테스트")
    void saveNotice() {
        // When
        NoticeEntity savedNotice = noticeRepository.save(testNotice);

        // Then
        assertThat(savedNotice).isNotNull();
        assertThat(savedNotice.getNoticeId()).isNotNull(); // UUID가 생성되었는지 확인
        assertThat(savedNotice.getTitle()).isEqualTo("Test Notice Title");
        assertThat(savedNotice.getContent()).isEqualTo("Test Notice Content");
        assertThat(savedNotice.getUser()).isEqualTo(testUser);
        assertThat(savedNotice.getClassLecture()).isEqualTo(testClassLecture);
    }

    @Test
    @DisplayName("공지사항 ID로 조회 테스트")
    void findById() {
        // Given
        NoticeEntity savedNotice = noticeRepository.save(testNotice);
        String noticeId = savedNotice.getNoticeId();

        // When
        Optional<NoticeEntity> foundNotice = noticeRepository.findById(noticeId);

        // Then
        assertThat(foundNotice).isPresent();
        assertThat(foundNotice.get().getTitle()).isEqualTo("Test Notice Title");
        assertThat(foundNotice.get().getContent()).isEqualTo("Test Notice Content");
    }

    @Test
    @DisplayName("모든 공지사항 조회 테스트")
    void findAll() {
        // Given
        noticeRepository.save(testNotice);

        NoticeEntity secondNotice = NoticeEntity.create(
                "Second Notice Title",
                "Second Notice Content",
                testUser,
                testClassLecture
        );
        noticeRepository.save(secondNotice);

        // When
        List<NoticeEntity> allNotices = noticeRepository.findAll();

        // Then
        assertThat(allNotices).hasSize(2);
        assertThat(allNotices).extracting(NoticeEntity::getTitle)
                .containsExactlyInAnyOrder("Test Notice Title", "Second Notice Title");
    }

    @Test
    @DisplayName("공지사항 삭제 테스트")
    void deleteNotice() {
        // Given
        NoticeEntity savedNotice = noticeRepository.save(testNotice);
        String noticeId = savedNotice.getNoticeId();

        // When
        noticeRepository.delete(savedNotice);
        Optional<NoticeEntity> deletedNotice = noticeRepository.findById(noticeId);

        // Then
        assertThat(deletedNotice).isEmpty();
    }

    @Test
    @DisplayName("공지사항 업데이트 테스트")
    void updateNotice() {
        // Given
        NoticeEntity savedNotice = noticeRepository.save(testNotice);
        String noticeId = savedNotice.getNoticeId();

        // When
        savedNotice.update(noticeId, "Updated Title", "Updated Content");
        noticeRepository.save(savedNotice);

        // Then
        Optional<NoticeEntity> updatedNotice = noticeRepository.findById(noticeId);
        assertThat(updatedNotice).isPresent();
        assertThat(updatedNotice.get().getTitle()).isEqualTo("Updated Title");
        assertThat(updatedNotice.get().getContent()).isEqualTo("Updated Content");
    }
} 