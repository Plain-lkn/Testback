package org.example.plain.domain.lecture.service;

import org.example.plain.common.ResponseField;
import org.example.plain.domain.lecture.dto.LectureResponse;
import org.example.plain.domain.lecture.entity.LectureEntity;
import org.example.plain.domain.lecture.entity.member.LectureMemberEntity;
import org.example.plain.domain.lecture.enums.LectureType;
import org.example.plain.domain.lecture.repository.LectureMemberRepository;
import org.example.plain.domain.lecture.repository.LectureRepository;
import org.example.plain.domain.lecture.service.impl.LectureMemberServiceImpl;
import org.example.plain.domain.user.dto.UserResponse;
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

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LectureMemberServiceTest {

    @InjectMocks
    private LectureMemberServiceImpl lectureMemberService;

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private LectureMemberRepository lectureMemberRepository;

    @Mock
    private UserRepository userRepository;

    private User testUser;
    private LectureEntity testLecture;
    private LectureMemberEntity testLectureMember;
    private String userId = "user123";
    private String lectureId = "lecture123";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(userId)
                .username("Test User")
                .build();

        testLecture = LectureEntity.builder()
                .id(lectureId)
                .user(User.builder().id("instructor123").username("Instructor").build())
                .lectureType(LectureType.VIDEO)
                .lectureName("Test Lecture")
                .lectureDescription("Test Description")
                .lecturePrice(10000)
                .build();

        testLectureMember = LectureMemberEntity.builder()
                .user(testUser)
                .lecture(testLecture)
                .build();
    }

    @Test
    @DisplayName("수강생 목록 조회 성공")
    void getStudentListSuccess() {
        // Given
        when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(testLecture));
        when(lectureMemberRepository.findByLectureId(lectureId)).thenReturn(List.of(testLectureMember));

        // When
        ResponseField<List<UserResponse>> result = lectureMemberService.getStudentList(lectureId);

        // Then
        assertEquals(HttpStatus.OK, result.getStatus());
        assertEquals(1, result.getBody().size());
        assertEquals(testUser.getId(), result.getBody().get(0).id());
    }

    @Test
    @DisplayName("수강생 목록 조회 실패 - 존재하지 않는 강의")
    void getStudentListFailNotFound() {
        // Given
        when(lectureRepository.findById(lectureId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> lectureMemberService.getStudentList(lectureId));
    }

    @Test
    @DisplayName("내 수강 목록 조회 성공")
    void getLectureMyListSuccess() {
        // Given
        when(lectureMemberRepository.findByUserId(userId)).thenReturn(List.of(testLectureMember));

        // When
        ResponseField<List<LectureResponse>> result = lectureMemberService.getLectureMyList(userId);

        // Then
        assertEquals(HttpStatus.OK, result.getStatus());
        assertEquals(1, result.getBody().size());
        assertEquals(testLecture.getId(), result.getBody().get(0).getLectureId());
    }

    @Test
    @DisplayName("내 수강 목록 조회 실패 - 수강 강의 없음")
    void getLectureMyListFailNoLectures() {
        // Given
        when(lectureMemberRepository.findByUserId(userId)).thenReturn(List.of());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> lectureMemberService.getLectureMyList(userId));
    }

    @Test
    @DisplayName("수강 신청 성공")
    void enrollLectureSuccess() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(testLecture));
        when(lectureMemberRepository.existsByLectureIdAndUserId(lectureId, userId)).thenReturn(false);

        // When
        ResponseField<Void> result = lectureMemberService.enrollLecture(lectureId, userId);

        // Then
        assertEquals(HttpStatus.OK, result.getStatus());
        verify(lectureMemberRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("수강 신청 실패 - 존재하지 않는 사용자")
    void enrollLectureFailUserNotFound() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> lectureMemberService.enrollLecture(lectureId, userId));
    }

    @Test
    @DisplayName("수강 신청 실패 - 존재하지 않는 강의")
    void enrollLectureFailLectureNotFound() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(lectureRepository.findById(lectureId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> lectureMemberService.enrollLecture(lectureId, userId));
    }

    @Test
    @DisplayName("수강 신청 실패 - 이미 수강 중인 강의")
    void enrollLectureFailAlreadyEnrolled() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(testLecture));
        when(lectureMemberRepository.existsByLectureIdAndUserId(lectureId, userId)).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> lectureMemberService.enrollLecture(lectureId, userId));
    }

    @Test
    @DisplayName("수강 취소 성공")
    void cancelEnrollmentSuccess() {
        // Given
        when(lectureMemberRepository.findByLectureIdAndUserId(lectureId, userId)).thenReturn(Optional.of(testLectureMember));

        // When
        ResponseField<Void> result = lectureMemberService.cancelEnrollment(lectureId, userId);

        // Then
        assertEquals(HttpStatus.OK, result.getStatus());
        verify(lectureMemberRepository, times(1)).delete(testLectureMember);
    }

    @Test
    @DisplayName("수강 취소 실패 - 수강 중이 아닌 강의")
    void cancelEnrollmentFailNotEnrolled() {
        // Given
        when(lectureMemberRepository.findByLectureIdAndUserId(lectureId, userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> lectureMemberService.cancelEnrollment(lectureId, userId));
    }
} 