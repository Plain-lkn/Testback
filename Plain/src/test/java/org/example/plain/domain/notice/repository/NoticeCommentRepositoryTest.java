package org.example.plain.domain.notice.repository;

import org.example.plain.domain.notice.entity.NoticeCommentEntity;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NoticeCommentRepositoryTest {

    @Autowired
    private NoticeCommentRepository noticeCommentRepository;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private NoticeCommentEntity testComment;

    @BeforeEach
    void setUp() {
        // Test User 생성
        testUser = User.builder()
                .id("test-user-id")
                .username("testuser")
                .email("test@example.com")
                .build();

        // 공지사항 ID 생성
        String noticeId = UUID.randomUUID().toString();

        // NoticeCommentEntity 생성
        testComment = NoticeCommentEntity.create(
                "Test Comment Title",
                "Test Comment Content",
                noticeId, // UUID 형식의 noticeId
                testUser
        );
    }

    @Test
    @DisplayName("댓글 저장 테스트")
    void saveComment() {
        // When
        NoticeCommentEntity savedComment = noticeCommentRepository.save(testComment);

        // Then
        assertThat(savedComment).isNotNull();
        assertThat(savedComment.getCommentId()).isNotNull(); // UUID가 생성되었는지 확인
        assertThat(savedComment.getTitle()).isEqualTo("Test Comment Title");
        assertThat(savedComment.getContent()).isEqualTo("Test Comment Content");
        assertThat(savedComment.getUser()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("댓글 ID로 조회 테스트")
    void findById() {
        // Given
        NoticeCommentEntity savedComment = noticeCommentRepository.save(testComment);
        String commentId = savedComment.getCommentId();

        // When
        Optional<NoticeCommentEntity> foundComment = noticeCommentRepository.findById(commentId);

        // Then
        assertThat(foundComment).isPresent();
        assertThat(foundComment.get().getTitle()).isEqualTo("Test Comment Title");
        assertThat(foundComment.get().getContent()).isEqualTo("Test Comment Content");
    }

    @Test
    @DisplayName("모든 댓글 조회 테스트")
    void findAll() {
        // Given
        noticeCommentRepository.save(testComment);

        NoticeCommentEntity secondComment = NoticeCommentEntity.create(
                "Second Comment Title",
                "Second Comment Content",
                UUID.randomUUID().toString(),
                testUser
        );
        noticeCommentRepository.save(secondComment);

        // When
        List<NoticeCommentEntity> allComments = noticeCommentRepository.findAll();

        // Then
        assertThat(allComments).hasSize(2);
        assertThat(allComments).extracting(NoticeCommentEntity::getTitle)
                .containsExactlyInAnyOrder("Test Comment Title", "Second Comment Title");
    }

    @Test
    @DisplayName("댓글 삭제 테스트")
    void deleteComment() {
        // Given
        NoticeCommentEntity savedComment = noticeCommentRepository.save(testComment);
        String commentId = savedComment.getCommentId();

        // When
        noticeCommentRepository.delete(savedComment);
        Optional<NoticeCommentEntity> deletedComment = noticeCommentRepository.findById(commentId);

        // Then
        assertThat(deletedComment).isEmpty();
    }

    @Test
    @DisplayName("댓글 업데이트 테스트")
    void updateComment() {
        // Given
        NoticeCommentEntity savedComment = noticeCommentRepository.save(testComment);
        String commentId = savedComment.getCommentId();
        String noticeId = savedComment.getNoticeId();

        // When
        savedComment.update(commentId, noticeId, "Updated Title", "Updated Content");
        noticeCommentRepository.save(savedComment);

        // Then
        Optional<NoticeCommentEntity> updatedComment = noticeCommentRepository.findById(commentId);
        assertThat(updatedComment).isPresent();
        assertThat(updatedComment.get().getTitle()).isEqualTo("Updated Title");
        assertThat(updatedComment.get().getContent()).isEqualTo("Updated Content");
    }
} 