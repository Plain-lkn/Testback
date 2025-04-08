package org.example.plain.domain.homework.service;

import org.example.plain.domain.classLecture.entity.ClassLecture;
import org.example.plain.domain.classMember.entity.ClassMember;
import org.example.plain.domain.classMember.entity.ClassMemberId;
import org.example.plain.domain.classMember.repository.ClassMemberRepository;
import org.example.plain.domain.file.dto.SubmitFileData;
import org.example.plain.domain.file.entity.FileEntity;
import org.example.plain.domain.file.interfaces.CloudFileService;
import org.example.plain.domain.homework.dto.Work;
import org.example.plain.domain.homework.dto.WorkSubmitField;
import org.example.plain.domain.homework.entity.WorkEntity;
import org.example.plain.domain.homework.entity.WorkMemberEntity;
import org.example.plain.domain.homework.entity.WorkMemberId;
import org.example.plain.domain.homework.interfaces.WorkService;
import org.example.plain.domain.homework.repository.FileRepository;
import org.example.plain.domain.user.entity.User;
import org.example.plain.repository.BoardRepository;
import org.example.plain.repository.WorkMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubmissionServiceImplTest {

    @Mock
    private CloudFileService fileService;

    @Mock
    private WorkMemberRepository workMemberRepository;

    @Mock
    private ClassMemberRepository groupMemberRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private WorkService workService;

    @Mock
    private FileRepository fileRepository;

    private SubmissionServiceImpl submissionService;

    private User testUser;
    private User testInstructor;
    private ClassLecture testClass;
    private ClassMember testClassMember;
    private WorkEntity testWork;
    private WorkMemberEntity testWorkMember;
    private List<FileEntity> testFiles;
    private WorkSubmitField testSubmitField;

    @BeforeEach
    void setUp() {
        submissionService = new SubmissionServiceImpl(fileService, workMemberRepository, groupMemberRepository, boardRepository, fileRepository);

        testUser = User.builder()
                .id("testUser")
                .username("Test User")
                .email("test@example.com")
                .build();

        testInstructor = User.builder()
                .id("testInstructor")
                .username("Test Instructor")
                .email("instructor@example.com")
                .build();

        testClass = ClassLecture.builder()
                .id("testClassId")
                .title("Test Class")
                .description("Test Description")
                .code("TEST123")
                .instructor(testInstructor)
                .build();

        testClassMember = new ClassMember(testClass, testUser);

        testWork = WorkEntity.builder()
                .classId(testClass.getId())
                .title("Test Work")
                .content("Test Content")
                .type("WORK")
                .userId(testInstructor.getId())
                .workId("testWorkId")
                .deadline(LocalDateTime.now().plusDays(1))
                .build();

        testWorkMember = WorkMemberEntity.builder()
                .workMemberId(new WorkMemberId("testWorkId", "testUser"))
                .work(testWork)
                .user(testUser)
                .isSubmited(false)
                .isLate(false)
                .fileEntities(new ArrayList<>())
                .build();

        testFiles = Arrays.asList(
            FileEntity.builder()
                    .filename("test1.txt")
                    .filePath("https://test-bucket.s3.amazonaws.com/test1.txt")
                    .build(),
            FileEntity.builder()
                    .filename("test2.txt")
                    .filePath("https://test-bucket.s3.amazonaws.com/test2.txt")
                    .build()
        );

        testSubmitField = WorkSubmitField.builder()
                .workId("testWorkId")
                .userId("testUser")
                .file(Arrays.asList(
                    new MockMultipartFile("file1", "test1.txt", "text/plain", "test1".getBytes()),
                    new MockMultipartFile("file2", "test2.txt", "text/plain", "test2".getBytes())
                ))
                .build();
    }

    @Test
    void submit_Success() {
        // given
        Work work = new Work();
        work.setWorkId("testWorkId");
        work.setDeadline(LocalDateTime.now().plusDays(1));

        when(workService.selectWork("testWorkId")).thenReturn(work);
        when(workMemberRepository.findById(any(WorkMemberId.class)))
                .thenReturn(Optional.of(testWorkMember));
        when(groupMemberRepository.findById(any(ClassMemberId.class)))
                .thenReturn(Optional.of(testClassMember));
        when(fileService.uploadFiles(any(SubmitFileData.class), any()))
                .thenReturn(testFiles);

        // when
        submissionService.submit(testSubmitField);

        // then
        verify(workMemberRepository).save(any(WorkMemberEntity.class));
        verify(fileService).uploadFiles(any(SubmitFileData.class), any());
        
        WorkMemberEntity savedEntity = testWorkMember;
        assertThat(savedEntity.isSubmited()).isTrue();
        assertThat(savedEntity.getFileEntities()).hasSize(2);
    }

    @Test
    void submit_NotClassMember_ThrowsException() {
        // given
        Work work = new Work();
        work.setWorkId("testWorkId");
        work.setDeadline(LocalDateTime.now().plusDays(1));

        when(workService.selectWork("testWorkId")).thenReturn(work);
        when(workMemberRepository.findById(any(WorkMemberId.class)))
                .thenReturn(Optional.of(testWorkMember));
        when(groupMemberRepository.findById(any(ClassMemberId.class)))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> submissionService.submit(testSubmitField))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("클래스 멤버가 아닙니다");

        verify(workMemberRepository, never()).save(any(WorkMemberEntity.class));
        verify(fileService, never()).uploadFiles(any(), any());
    }

    @Test
    void submit_DeadlinePassed_ThrowsException() {
        // given
        Work work = new Work();
        work.setWorkId("testWorkId");
        work.setDeadline(LocalDateTime.now().minusDays(1));

        when(workService.selectWork("testWorkId")).thenReturn(work);
        when(workMemberRepository.findById(any(WorkMemberId.class)))
                .thenReturn(Optional.of(testWorkMember));
        when(groupMemberRepository.findById(any(ClassMemberId.class)))
                .thenReturn(Optional.of(testClassMember));

        // when & then
        assertThatThrownBy(() -> submissionService.submit(testSubmitField))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("과제 제출 기한이 지났습니다");

        verify(workMemberRepository, never()).save(any(WorkMemberEntity.class));
        verify(fileService, never()).uploadFiles(any(), any());
    }

    @Test
    void getSubmissionFiles_Success() {
        // given
        testWorkMember.setFileEntities(testFiles);
        when(workMemberRepository.findById(any(WorkMemberId.class)))
                .thenReturn(Optional.of(testWorkMember));
        when(groupMemberRepository.findById(any(ClassMemberId.class)))
                .thenReturn(Optional.of(testClassMember));

        // when
        List<String> result = submissionService.getSubmissionFiles("testWorkId", "testUser");

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(
            "https://test-bucket.s3.amazonaws.com/test1.txt",
            "https://test-bucket.s3.amazonaws.com/test2.txt"
        );
    }

    @Test
    void getSubmissionFiles_NotClassMember_ThrowsException() {
        // given
        when(workMemberRepository.findById(any(WorkMemberId.class)))
                .thenReturn(Optional.of(testWorkMember));
        when(groupMemberRepository.findById(any(ClassMemberId.class)))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> submissionService.getSubmissionFiles("testWorkId", "testUser"))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("클래스 멤버가 아닙니다");
    }

    @Test
    void getSubmissionFiles_NotFound_ThrowsException() {
        // given
        when(workMemberRepository.findById(any(WorkMemberId.class)))
                .thenReturn(Optional.empty());
        when(groupMemberRepository.findById(any(ClassMemberId.class)))
                .thenReturn(Optional.of(testClassMember));

        // when & then
        assertThatThrownBy(() -> submissionService.getSubmissionFiles("testWorkId", "testUser"))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("과제 제출 정보를 찾을 수 없습니다");
    }

    @Test
    void cancelSubmission_Success() {
        // given
        testWorkMember.setFileEntities(testFiles);
        when(workMemberRepository.findById(any(WorkMemberId.class)))
                .thenReturn(Optional.of(testWorkMember));
        when(groupMemberRepository.findById(any(ClassMemberId.class)))
                .thenReturn(Optional.of(testClassMember));

        // when
        submissionService.cancelSubmission("testWorkId", "testUser");

        // then
        verify(fileService, times(2)).deleteFile(any(String.class));
        verify(workMemberRepository).save(any(WorkMemberEntity.class));
        
        WorkMemberEntity savedEntity = testWorkMember;
        assertThat(savedEntity.isSubmited()).isFalse();
        assertThat(savedEntity.getFileEntities()).isEmpty();
    }

    @Test
    void cancelSubmission_NotClassMember_ThrowsException() {
        // given
        when(workMemberRepository.findById(any(WorkMemberId.class)))
                .thenReturn(Optional.of(testWorkMember));
        when(groupMemberRepository.findById(any(ClassMemberId.class)))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> submissionService.cancelSubmission("testWorkId", "testUser"))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("클래스 멤버가 아닙니다");

        verify(fileService, never()).deleteFile(any(String.class));
        verify(workMemberRepository, never()).save(any(WorkMemberEntity.class));
    }

    @Test
    void isSubmitted_Success() {
        // given
        testWorkMember.setSubmited(true);
        when(workMemberRepository.findById(any(WorkMemberId.class)))
                .thenReturn(Optional.of(testWorkMember));
        when(groupMemberRepository.findById(any(ClassMemberId.class)))
                .thenReturn(Optional.of(testClassMember));

        // when
        boolean result = submissionService.isSubmitted("testWorkId", "testUser");

        // then
        assertThat(result).isTrue();
    }

    @Test
    void isSubmitted_NotClassMember_ThrowsException() {
        // given
        when(workMemberRepository.findById(any(WorkMemberId.class)))
                .thenReturn(Optional.of(testWorkMember));
        when(groupMemberRepository.findById(any(ClassMemberId.class)))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> submissionService.isSubmitted("testWorkId", "testUser"))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("클래스 멤버가 아닙니다");
    }

    @Test
    void isSubmitted_NotFound_ReturnsFalse() {
        // given
        when(workMemberRepository.findById(any(WorkMemberId.class)))
                .thenReturn(Optional.empty());
        when(groupMemberRepository.findById(any(ClassMemberId.class)))
                .thenReturn(Optional.of(testClassMember));

        // when
        boolean result = submissionService.isSubmitted("testWorkId", "testUser");

        // then
        assertThat(result).isFalse();
    }
} 