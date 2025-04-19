package org.example.plain.user.service;

import org.example.plain.domain.user.dto.UserRequest;
import org.example.plain.domain.user.dto.UserResponse;
import org.example.plain.domain.user.entity.EmailVerification;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.interfaces.UserService;
import org.example.plain.domain.user.repository.EmailVerificationRepository;
import org.example.plain.domain.user.repository.UserRepository;
import org.example.plain.domain.user.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.example.plain.domain.file.interfaces.CloudFileService;
import org.example.plain.domain.file.interfaces.FileDatabaseService;
import org.example.plain.domain.file.repository.UserFileRepository;
import org.example.plain.domain.file.dto.FileInfo;
import org.example.plain.domain.file.entity.FileEntity;
import org.example.plain.domain.file.entity.ImageFileEntity;
import org.example.plain.domain.file.entity.UserFileEntity;
import org.springframework.mock.web.MockMultipartFile;

@SpringJUnitConfig
public class UserServiceTest {

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @MockBean
    private CloudFileService cloudFileService;
    @MockBean
    private FileDatabaseService userImageService;
    @MockBean
    private UserFileRepository userFileRepository;
    @MockBean
    private EmailVerificationRepository emailVerificationRepository;
    
    private UserService userService;
    private List<UserRequest> userRequestRespons;

    @BeforeEach
    public void setUp() {
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(null);

        userRequestRespons = new ArrayList<>();

        UserRequest userRequest = new UserRequest("kimsee","박근택","go@email.com","1234");
        UserRequest userRequest2 = new UserRequest("김주호","ju@email");
        UserRequest userRequest3 = new UserRequest("rkedx","김갑든","kimgap@gmail.com","1234");
        UserRequest userRequest4 = new UserRequest("김주호","opt@email.com");

        userRequestRespons.add(userRequest);
        userRequestRespons.add(userRequest2);
        userRequestRespons.add(userRequest3);
        userRequestRespons.add(userRequest4);
        
        // Initialize userService with all required dependencies
        userService = new UserServiceImpl(
            userRepository,
            bCryptPasswordEncoder,
            cloudFileService,
            userImageService,
            userFileRepository,
            emailVerificationRepository
        );
    }

    @Test
    public void testSave() {
        // 이메일 인증 mock 설정
        EmailVerification verification = EmailVerification.builder()
                .email("go@email.com")
                .code("123456")
                .verified(true)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .build();
        Mockito.when(emailVerificationRepository.findById("go@email.com"))
               .thenReturn(Optional.of(verification));
        
        boolean result = userService.createUser(userRequestRespons.get(0));
        assertThat(result).isTrue();
    }

    @Test
    public void testSave_EmailNotVerified() {
        // 이메일 인증되지 않은 경우 mock 설정
        EmailVerification verification = EmailVerification.builder()
                .email("go@email.com")
                .code("123456")
                .verified(false)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .build();
        Mockito.when(emailVerificationRepository.findById("go@email.com"))
               .thenReturn(Optional.of(verification));
        
        assertThrows(HttpClientErrorException.class, () -> {
            userService.createUser(userRequestRespons.get(0));
        });
    }

    @Test
    public void testSave_EmailVerificationNotFound() {
        // 이메일 인증 정보가 없는 경우 mock 설정
        Mockito.when(emailVerificationRepository.findById("go@email.com"))
               .thenReturn(Optional.empty());
        
        assertThrows(NoSuchElementException.class, () -> {
            userService.createUser(userRequestRespons.get(0));
        });
    }

    @Test
    public void testGet(){
        Mockito.when(userRepository.findById(Mockito.eq("kimsee"))).thenReturn(Optional.of(new User(userRequestRespons.get(0))));
        Mockito.when(userRepository.findByEmail(Mockito.eq("go@email.com"))).thenReturn(Optional.of(new User(userRequestRespons.get(0))));

        UserResponse userRequest = userService.getUserByEmail("go@email.com");
        UserResponse userRequest2 = userService.getUser("kimsee");

        assertThat(userRequest).isNotNull();
        assertThat(userRequest2).isNotNull();
        assertThat(userRequest).isEqualTo(userRequest2);
        assertThat(userRequest.email()).isEqualTo("go@email.com");
        assertThat(userRequest.username()).isEqualTo("박근택");
    }

    @Test
    public void testRemove(){
        Mockito.when(userRepository.findById(Mockito.eq("kimsee"))).thenReturn(Optional.of(new User(userRequestRespons.get(0))));
        Mockito.when(userRepository.findByEmail(Mockito.eq("go@email.com"))).thenReturn(Optional.of(new User(userRequestRespons.get(0))));

        boolean result = userService.deleteUser("kimsee");

        assertThat(result).isTrue();
    }

    @Test
    public void testUpdate(){
        Mockito.when(userRepository.findById(Mockito.eq("kimsee"))).thenReturn(Optional.of(new User(userRequestRespons.get(0))));
        Mockito.when(userRepository.findByEmail(Mockito.eq("go@email.com"))).thenReturn(Optional.of(new User(userRequestRespons.get(0))));

        boolean result = userService.updateUser(new UserRequest("kimsee","kim@email.com",null,null));

        assertThat(result).isTrue();
    }
    
    @Test
    public void testUpdateProfileImage() {
        // 테스트용 프로필 이미지 파일 생성
        MockMultipartFile imageFile = new MockMultipartFile(
            "profileImage", 
            "test-image.jpg", 
            "image/jpeg", 
            "테스트 이미지 데이터".getBytes()
        );
        
        // 사용자 ID
        String userId = "kimsee";
        
        // 필요한 mock 설정
        // 1. 사용자 조회 mock
        User mockUser = new User(userRequestRespons.get(0));
        Mockito.when(userRepository.findById(userId))
               .thenReturn(Optional.of(mockUser));
        
        // 2. 파일 업로드 mock (CloudFileService)
        FileInfo mockFileInfo = new FileInfo("test-image.jpg", "https://example.com/images/test-image.jpg");
        Mockito.when(cloudFileService.uploadSingleFile(Mockito.any(), Mockito.eq(userId), Mockito.eq("profile")))
               .thenReturn(mockFileInfo);
        
        // 3. 파일 저장 mock (FileDatabaseService)
        // FileDatabaseService.save는 FileEntity를 반환하므로 doNothing 대신 when().thenReturn() 사용
        ImageFileEntity mockImageFile = ImageFileEntity.makeImageFileEntity(
            mockFileInfo.getFilename(), mockFileInfo.getFileUrl()
        );
        Mockito.when(userImageService.save(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any()
        )).thenReturn(null);
        
        // 프로필 이미지 업데이트 실행
        boolean result = userService.updateProfileImage(imageFile, userId);
        
        // 결과 검증
        assertThat(result).isTrue();
        
        // userRepository.findById가 호출되었는지 확인
        Mockito.verify(userRepository).findById(userId);
        
        // cloudFileService.uploadSingleFile이 호출되었는지 확인
        Mockito.verify(cloudFileService).uploadSingleFile(Mockito.any(), Mockito.eq(userId), Mockito.eq("profile"));
        
        // userImageService.save가 호출되었는지 확인
        Mockito.verify(userImageService).save(
            Mockito.eq(mockFileInfo.getFilename()), 
            Mockito.eq(mockFileInfo.getFileUrl()), 
            Mockito.any()
        );
    }
    
    @Test
    public void testUpdateProfileImage_UserNotFound() {
        // 테스트용 프로필 이미지 파일 생성
        MockMultipartFile imageFile = new MockMultipartFile(
            "profileImage", 
            "test-image.jpg", 
            "image/jpeg", 
            "테스트 이미지 데이터".getBytes()
        );
        
        // 존재하지 않는 사용자 ID
        String userId = "non-existent-user";
        
        // 사용자를 찾을 수 없음을 설정
        Mockito.when(userRepository.findById(userId))
               .thenReturn(Optional.empty());
        
        // 사용자를 찾을 수 없는 경우 예외 발생 검증
        try {
            userService.updateProfileImage(imageFile, userId);
            // 예외가 발생하지 않으면 테스트 실패
            assertThat(false).isTrue();
        } catch (HttpClientErrorException e) {
            // 예외 검증
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(e.getMessage()).contains("사용자를 찾을 수 없습니다");
        }
        
        // userRepository.findById만 호출되고 다른 메소드는 호출되지 않아야 함
        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(cloudFileService, Mockito.never()).uploadSingleFile(Mockito.any(), Mockito.anyString(), Mockito.anyString());
        Mockito.verify(userImageService, Mockito.never()).save(Mockito.anyString(), Mockito.anyString(), Mockito.any());
    }
    
    @Test
    public void testGetUserWithProfileImage() {
        // 테스트 데이터 설정
        String userId = "kimsee";
        User mockUser = new User(userRequestRespons.get(0));
        
        // 이미지 파일 엔티티 생성
        ImageFileEntity mockImageFile = ImageFileEntity.makeImageFileEntity(
            "test-image.jpg", 
            "https://example.com/images/test-image.jpg"
        );
        
        // UserFileEntity 생성 (사용자와 이미지 연결)
        UserFileEntity mockUserFile = UserFileEntity.createUserProfileImage(mockUser, mockImageFile);
        
        // Mock 설정
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        Mockito.when(userFileRepository.findByUserId(userId)).thenReturn(Optional.of(mockUserFile));
        
        // 서비스 메소드 호출
        UserResponse response = userService.getUser(userId);
        
        // 검증
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.username()).isEqualTo("박근택");
        assertThat(response.email()).isEqualTo("go@email.com");
        assertThat(response.profileImageUrl()).isEqualTo("https://example.com/images/test-image.jpg");
        
        // 메소드 호출 검증
        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(userFileRepository).findByUserId(userId);
    }
}
