package org.example.plain.domain.lecture.service;

import org.example.plain.common.ResponseField;
import org.example.plain.domain.lecture.dto.LectureRequest;
import org.example.plain.domain.lecture.dto.LectureResponse;
import org.example.plain.domain.lecture.entity.LectureEntity;
import org.example.plain.domain.lecture.enums.LectureType;
import org.example.plain.domain.lecture.repository.LectureRepository;
import org.example.plain.domain.lecture.service.impl.LectureServiceImpl;
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
public class LectureServiceTest {

    @InjectMocks
    private LectureServiceImpl lectureService;

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private UserRepository userRepository;

    private User testUser;
    private LectureEntity testLecture;
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
                .user(testUser)
                .lectureType(LectureType.VIDEO)
                .lectureName("Test Lecture")
                .lectureDescription("Test Description")
                .lecturePrice(10000)
                .build();
    }

    @Test
    @DisplayName("강의 목록 조회 성공")
    void getLectureListSuccess() {
        // Given
        List<LectureEntity> lectures = Arrays.asList(testLecture);
        when(lectureRepository.findAll()).thenReturn(lectures);

        // When
        ResponseField<List<LectureResponse>> result = lectureService.getLectureList();

        // Then
        assertEquals(HttpStatus.OK, result.getStatus());
        assertEquals(1, result.getBody().size());
        assertEquals(testLecture.getId(), result.getBody().get(0).getLectureId());
    }

    @Test
    @DisplayName("강의 목록 조회 실패 - 강의 없음")
    void getLectureListFailNoLectures() {
        // Given
        when(lectureRepository.findAll()).thenReturn(List.of());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> lectureService.getLectureList());
    }

    @Test
    @DisplayName("강의 상세 조회 성공")
    void getLectureDetailSuccess() {
        // Given
        when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(testLecture));

        // When
        ResponseField<LectureResponse> result = lectureService.getLectureDetail(lectureId);

        // Then
        assertEquals(HttpStatus.OK, result.getStatus());
        assertEquals(testLecture.getId(), result.getBody().getLectureId());
        assertEquals(testLecture.getLectureName(), result.getBody().getLectureName());
    }

    @Test
    @DisplayName("강의 상세 조회 실패 - 존재하지 않는 강의")
    void getLectureDetailFailNotFound() {
        // Given
        when(lectureRepository.findById(lectureId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> lectureService.getLectureDetail(lectureId));
    }

    @Test
    @DisplayName("강의 생성 성공")
    void createLectureSuccess() {
        // Given
        LectureRequest request = new LectureRequest(
                LectureType.VIDEO,
                "New Lecture",
                "New Description",
                20000
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(lectureRepository.save(any(LectureEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ResponseField<LectureResponse> result = lectureService.createLecture(request, userId);

        // Then
        assertEquals(HttpStatus.OK, result.getStatus());
        assertEquals(request.getLectureName(), result.getBody().getLectureName());
        assertEquals(request.getLectureDescription(), result.getBody().getLectureDescription());
        verify(lectureRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("강의 수정 성공")
    void updateLectureSuccess() {
        // Given
        LectureRequest request = new LectureRequest(
                LectureType.LIVE,
                "Updated Lecture",
                "Updated Description",
                30000
        );

        when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(testLecture));

        // When
        ResponseField<LectureResponse> result = lectureService.updateLecture(lectureId, request, userId);

        // Then
        assertEquals(HttpStatus.OK, result.getStatus());
        assertEquals(request.getLectureName(), result.getBody().getLectureName());
        assertEquals(request.getLectureDescription(), result.getBody().getLectureDescription());
        assertEquals(request.getLecturePrice(), result.getBody().getLecturePrice());
    }

    @Test
    @DisplayName("강의 수정 실패 - 존재하지 않는 강의")
    void updateLectureFailNotFound() {
        // Given
        LectureRequest request = new LectureRequest(
                LectureType.VIDEO,
                "Updated Lecture",
                "Updated Description",
                30000
        );
        when(lectureRepository.findById(lectureId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> lectureService.updateLecture(lectureId, request, userId));
    }

    @Test
    @DisplayName("강의 수정 실패 - 권한 없음")
    void updateLectureFailNoPermission() {
        // Given
        LectureRequest request = new LectureRequest(
                LectureType.VIDEO,
                "Updated Lecture",
                "Updated Description",
                30000
        );
        when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(testLecture));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> lectureService.updateLecture(lectureId, request, "different-user"));
    }

    @Test
    @DisplayName("강의 삭제 성공")
    void deleteLectureSuccess() {
        // Given
        when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(testLecture));

        // When
        lectureService.deleteLecture(lectureId, userId);

        // Then
        verify(lectureRepository, times(1)).delete(testLecture);
    }

    @Test
    @DisplayName("강의 삭제 실패 - 존재하지 않는 강의")
    void deleteLectureFailNotFound() {
        // Given
        when(lectureRepository.findById(lectureId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> lectureService.deleteLecture(lectureId, userId));
    }

    @Test
    @DisplayName("강의 삭제 실패 - 권한 없음")
    void deleteLectureFailNoPermission() {
        // Given
        when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(testLecture));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> lectureService.deleteLecture(lectureId, "different-user"));
    }
} 