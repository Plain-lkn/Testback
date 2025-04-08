package org.example.plain.domain.file.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.example.plain.domain.file.dto.SubmitFileData;
import org.example.plain.domain.file.entity.FileEntity;
import org.example.plain.domain.homework.entity.WorkEntity;
import org.example.plain.domain.homework.entity.WorkMemberEntity;
import org.example.plain.domain.homework.repository.FileRepository;
import org.example.plain.domain.user.entity.User;
import org.example.plain.repository.WorkMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AwsFileServiceImplTest {
    private static final String BUCKET_NAME = "plain-test-s3";
    private static final String S3_BASE_URL = "https://" + BUCKET_NAME + ".s3.amazonaws.com/";
    private static final String FILE_PATH = "/tmp/";

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private WorkMemberRepository workMemberRepository;

    private AwsFileServiceImpl awsFileService;

    private User testUser;
    private WorkEntity testWork;
    private SubmitFileData testFileData;
    private List<MultipartFile> testFiles;
    private MultipartFile testFile;
    private WorkMemberEntity testWorkMember;

    @BeforeEach
    void setUp() {
        awsFileService = new AwsFileServiceImpl(amazonS3, fileRepository, workMemberRepository);
        
        // Set bucket value using ReflectionTestUtils
        ReflectionTestUtils.setField(awsFileService, "bucket", BUCKET_NAME);
        ReflectionTestUtils.setField(awsFileService, "filePath", FILE_PATH);
        
        testUser = User.builder()
                .id("testUser")
                .username("Test User")
                .email("test@example.com")
                .build();

        testWork = WorkEntity.builder()
                .workId("testWorkId")
                .classId("testClassId")
                .title("Test Work")
                .content("Test Content")
                .type("WORK")
                .userId(testUser.getId())
                .deadline(LocalDateTime.now().plusDays(1))
                .build();

        testWorkMember = WorkMemberEntity.makeWorkMemberEntity(testUser, testWork);

        testFile = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "test content".getBytes()
        );
        
        testFileData = SubmitFileData.builder()
                .userId(testUser)
                .workId(testWork)
                .filename("test.txt")
                .file(testFile)
                .build();

        testFiles = Arrays.asList(
            new MockMultipartFile("file1", "test1.txt", "text/plain", "test1".getBytes()),
            new MockMultipartFile("file2", "test2.txt", "text/plain", "test2".getBytes())
        );
    }

    @Test
    void uploadSingleFile_Success() {
        // given
        when(workMemberRepository.findByWorkIdAndUserId(anyString(), anyString()))
            .thenReturn(Optional.of(testWorkMember));

        FileEntity expectedFileEntity = FileEntity.builder()
                .filename("test.txt")
                .filePath(S3_BASE_URL + "test.txt")
                .workMember(testWorkMember)
                .build();

        when(fileRepository.save(any(FileEntity.class))).thenReturn(expectedFileEntity);

        // when
        FileEntity result = awsFileService.uploadSingleFile(testFileData);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFilename()).isEqualTo("test.txt");
        assertThat(result.getWorkMember()).isEqualTo(testWorkMember);

        verify(workMemberRepository).findByWorkIdAndUserId(testWork.getWorkId(), testUser.getId());
        verify(amazonS3).putObject(any(PutObjectRequest.class));
        verify(fileRepository).save(any(FileEntity.class));
    }

    @Test
    void uploadSingleFile_WorkMemberNotFound_ThrowsException() {
        // given
        when(workMemberRepository.findByWorkIdAndUserId(anyString(), anyString()))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> awsFileService.uploadSingleFile(testFileData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("WorkMember not found");

        verify(workMemberRepository).findByWorkIdAndUserId(testWork.getWorkId(), testUser.getId());
        verify(amazonS3, never()).putObject(any(PutObjectRequest.class));
        verify(fileRepository, never()).save(any(FileEntity.class));
    }

    @Test
    void uploadFiles_Success() {
        // given
        when(workMemberRepository.findByWorkIdAndUserId(anyString(), anyString()))
            .thenReturn(Optional.of(testWorkMember));

        FileEntity expectedFileEntity1 = FileEntity.builder()
                .filename("test1.txt")
                .filePath(S3_BASE_URL + "test1.txt")
                .workMember(testWorkMember)
                .build();

        FileEntity expectedFileEntity2 = FileEntity.builder()
                .filename("test2.txt")
                .filePath(S3_BASE_URL + "test2.txt")
                .workMember(testWorkMember)
                .build();

        when(fileRepository.save(any(FileEntity.class)))
                .thenReturn(expectedFileEntity1)
                .thenReturn(expectedFileEntity2);

        // when
        List<FileEntity> results = awsFileService.uploadFiles(testFileData, testFiles);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getFilename()).isEqualTo("test1.txt");
        assertThat(results.get(1).getFilename()).isEqualTo("test2.txt");

        verify(workMemberRepository).findByWorkIdAndUserId(testWork.getWorkId(), testUser.getId());
        verify(amazonS3, times(2)).putObject(any(PutObjectRequest.class));
        verify(fileRepository, times(2)).save(any(FileEntity.class));
    }

    @Test
    void uploadFiles_WorkMemberNotFound_ThrowsException() {
        // given
        when(workMemberRepository.findByWorkIdAndUserId(anyString(), anyString()))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> awsFileService.uploadFiles(testFileData, testFiles))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("WorkMember not found");

        verify(workMemberRepository).findByWorkIdAndUserId(testWork.getWorkId(), testUser.getId());
        verify(amazonS3, never()).putObject(any(PutObjectRequest.class));
        verify(fileRepository, never()).save(any(FileEntity.class));
    }

    @Test
    void deleteFile_Success() {
        // given
        String fileUrl = S3_BASE_URL + "test.txt";

        // when
        awsFileService.deleteFile(fileUrl);

        // then
        ArgumentCaptor<DeleteObjectRequest> deleteRequestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(amazonS3).deleteObject(deleteRequestCaptor.capture());

        DeleteObjectRequest deleteRequest = deleteRequestCaptor.getValue();
        assertThat(deleteRequest.getBucketName()).isEqualTo(BUCKET_NAME);
        assertThat(deleteRequest.getKey()).isEqualTo("test.txt");
    }

    @Test
    void deleteFile_WithPath_Success() {
        // given
        String fileUrl = S3_BASE_URL + "testUser/testWorkId/test.txt";

        // when
        awsFileService.deleteFile(fileUrl);

        // then
        ArgumentCaptor<DeleteObjectRequest> deleteRequestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(amazonS3).deleteObject(deleteRequestCaptor.capture());

        DeleteObjectRequest deleteRequest = deleteRequestCaptor.getValue();
        assertThat(deleteRequest.getBucketName()).isEqualTo(BUCKET_NAME);
        assertThat(deleteRequest.getKey()).isEqualTo("testUser/testWorkId/test.txt");
    }

    @Test
    void deleteFile_InvalidUrl_ThrowsException() {
        // given
        String invalidUrl = "invalid-url";

        // when & then
        assertThatThrownBy(() -> awsFileService.deleteFile(invalidUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid S3 URL");

        verify(amazonS3, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void deleteFile_NullUrl_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> awsFileService.deleteFile(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File URL cannot be null or empty");

        verify(amazonS3, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void deleteFile_EmptyUrl_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> awsFileService.deleteFile(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File URL cannot be null or empty");

        verify(amazonS3, never()).deleteObject(any(DeleteObjectRequest.class));
    }
} 