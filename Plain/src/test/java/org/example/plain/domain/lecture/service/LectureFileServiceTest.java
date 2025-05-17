package org.example.plain.domain.lecture.service;

import org.example.plain.common.ResponseField;
import org.example.plain.domain.file.dto.FileInfo;
import org.example.plain.domain.file.interfaces.CloudFileService;
import org.example.plain.domain.file.interfaces.FileDatabaseService;
import org.example.plain.domain.lecture.dto.LectureFileResponse;
import org.example.plain.domain.lecture.entity.LectureEntity;
import org.example.plain.domain.lecture.entity.LectureFileEntity;
import org.example.plain.domain.lecture.enums.LectureType;
import org.example.plain.domain.lecture.repository.LectureFileRepository;
import org.example.plain.domain.lecture.repository.LectureRepository;
import org.example.plain.domain.lecture.service.impl.LectureFileBusinessImpl;
import org.example.plain.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LectureFileServiceTest {

    @InjectMocks
    private LectureFileBusinessImpl lectureFileService;

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private LectureFileRepository lectureFileRepository;
    
    @Mock
    private CloudFileService cloudFileService;
    
    @Mock
    private FileDatabaseService fileDatabaseService;

    private User instructor;
    private LectureEntity lecture;
    private LectureFileEntity lectureFile;
    private String userId = "user123";
    private String lectureId = "lecture123";
    private String fileId = "file123";

    @BeforeEach
    void setUp() {
        instructor = User.builder()
                .id(userId)
                .username("Test Instructor")
                .build();

        lecture = LectureEntity.builder()
                .id(lectureId)
                .user(instructor)
                .lectureType(LectureType.VIDEO)
                .lectureName("Test Lecture")
                .lectureDescription("Test Description")
                .lecturePrice(10000)
                .build();

        lectureFile = LectureFileEntity.builder()
                .id(fileId)
                .lecture(lecture)
                .filename("test-file.pdf")
                .filePath("https://example.com/files/test-file.pdf")
                .build();
    }

    @Test
    @DisplayName("강의 파일 업로드 성공")
    void uploadLectureFileSuccess() throws IOException {
        // Given
        MultipartFile file = new MockMultipartFile(
                "file", 
                "test-file.pdf", 
                "application/pdf", 
                "test content".getBytes()
        );
        
        FileInfo fileInfo = new FileInfo("test-file.pdf", "https://example.com/files/test-file.pdf");
        
        when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(lecture));
        when(cloudFileService.uploadSingleFile(any(), anyString())).thenReturn(fileInfo);
        
        // When
        ResponseField<String> result = lectureFileService.uploadLectureFile(lectureId, file);
        
        // Then
        assertEquals(HttpStatus.OK, result.getStatus());
        assertEquals(fileInfo.getFileUrl(), result.getBody());
        verify(fileDatabaseService, times(1)).save(anyString(), anyString(), any());
    }
    
    @Test
    @DisplayName("강의 파일 업로드 실패 - 존재하지 않는 강의")
    void uploadLectureFileFailLectureNotFound() throws IOException {
        // Given
        MultipartFile file = new MockMultipartFile(
                "file", 
                "test-file.pdf", 
                "application/pdf", 
                "test content".getBytes()
        );
        
        when(lectureRepository.findById(lectureId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> lectureFileService.uploadLectureFile(lectureId, file));
    }
    
    @Test
    @DisplayName("강의 파일 조회 성공")
    void getLectureFileSuccess() {
        // Given
        when(lectureFileRepository.findByIdAndLectureId(fileId, lectureId)).thenReturn(Optional.of(lectureFile));
        
        // When
        ResponseField<String> result = lectureFileService.getLectureFile(lectureId, fileId);
        
        // Then
        assertEquals(HttpStatus.OK, result.getStatus());
        assertEquals(lectureFile.getFilePath(), result.getBody());
    }
    
    @Test
    @DisplayName("강의 파일 조회 실패 - 존재하지 않는 파일")
    void getLectureFileFailFileNotFound() {
        // Given
        when(lectureFileRepository.findByIdAndLectureId(fileId, lectureId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> lectureFileService.getLectureFile(lectureId, fileId));
    }
    
    @Test
    @DisplayName("강의 파일 목록 조회 성공")
    void getLectureFilesSuccess() {
        // Given
        List<LectureFileEntity> files = Arrays.asList(lectureFile);
        when(lectureFileRepository.findByLectureId(lectureId)).thenReturn(files);
        
        // When
        ResponseField<List<LectureFileResponse>> result = lectureFileService.getLectureFiles(lectureId);
        
        // Then
        assertEquals(HttpStatus.OK, result.getStatus());
        assertEquals(1, result.getBody().size());
    }
    
    @Test
    @DisplayName("강의 파일 삭제 성공")
    void deleteLectureFileSuccess() {
        // Given
        when(lectureFileRepository.findByIdAndLectureId(fileId, lectureId)).thenReturn(Optional.of(lectureFile));
        
        // When
        ResponseField<Void> result = lectureFileService.deleteLectureFile(lectureId, fileId);
        
        // Then
        assertEquals(HttpStatus.OK, result.getStatus());
        verify(cloudFileService, times(1)).deleteFile((LectureFileEntity) any());
        verify(fileDatabaseService, times(1)).delete(any());
    }
    
    @Test
    @DisplayName("강의 파일 삭제 실패 - 존재하지 않는 파일")
    void deleteLectureFileFailFileNotFound() {
        // Given
        when(lectureFileRepository.findByIdAndLectureId(fileId, lectureId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> lectureFileService.deleteLectureFile(lectureId, fileId));
    }
} 