package org.example.plain.domain.user.integration;

import org.example.plain.common.enums.Role;
import org.example.plain.domain.user.dto.UserRequest;
import org.example.plain.domain.user.dto.UserResponse;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.interfaces.UserService;
import org.example.plain.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class UserIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private static final String TEST_USER_ID = "testuser";
    private static final String TEST_USERNAME = "Test User";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";

    private UserRequest testUserRequest;

    @BeforeEach
    void setUp() {
        // 테스트용 유저 요청 객체 생성
        testUserRequest = UserRequest.builder()
                .id(TEST_USER_ID)
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();

        // 이전 테스트에서 남은 유저가 있으면 삭제
        userRepository.findById(TEST_USER_ID)
                .ifPresent(user -> userRepository.delete(user));
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 유저 삭제
        userRepository.findById(TEST_USER_ID)
                .ifPresent(user -> userRepository.delete(user));
    }

    @Nested
    @DisplayName("유저 계정 생성 테스트")
    class UserCreationTest {
        
        @Test
        @DisplayName("유저 생성 성공")
        void createUser_Success() {
            // when
            boolean result = userService.createUser(testUserRequest);

            // then
            assertThat(result).isTrue();
            
            User savedUser = userRepository.findById(TEST_USER_ID)
                    .orElseThrow(() -> new RuntimeException("유저가 저장되지 않았습니다."));
            
            assertThat(savedUser.getId()).isEqualTo(TEST_USER_ID);
            assertThat(savedUser.getUsername()).isEqualTo(TEST_USERNAME);
            assertThat(savedUser.getEmail()).isEqualTo(TEST_EMAIL);
            // 비밀번호는 암호화되어 저장되므로 직접 비교하지 않음
        }

        @Test
        @DisplayName("유저 생성 실패 - 중복 ID")
        void createUser_Fail_DuplicateId() {
            // given
            userService.createUser(testUserRequest);

            // when & then
            UserRequest duplicateRequest = UserRequest.builder()
                    .id(TEST_USER_ID)
                    .username("Another User")
                    .email("another@example.com")
                    .password("anotherpassword")
                    .build();

            // 중복된 ID로 가입 시도
            assertThrows(Exception.class, () -> 
                userService.createUser(duplicateRequest)
            );
        }

        @Test
        @DisplayName("유저 생성 실패 - 중복 이메일")
        void createUser_Fail_DuplicateEmail() {
            // given
            userService.createUser(testUserRequest);

            // when & then
            UserRequest duplicateRequest = UserRequest.builder()
                    .id("anotheruser")
                    .username("Another User")
                    .email(TEST_EMAIL) // 같은 이메일 사용
                    .password("anotherpassword")
                    .build();

            // 중복된 이메일로 가입 시도
            assertThrows(Exception.class, () -> 
                userService.createUser(duplicateRequest)
            );
        }
    }

    @Nested
    @DisplayName("유저 정보 조회 테스트")
    class UserRetrievalTest {
        
        @BeforeEach
        void createTestUser() {
            userService.createUser(testUserRequest);
        }
        
        @Test
        @DisplayName("ID로 유저 조회 성공")
        void getUser_Success() {
            // when
            UserResponse userResponse = userService.getUser(TEST_USER_ID);

            // then
            assertThat(userResponse).isNotNull();
            assertThat(userResponse.id()).isEqualTo(TEST_USER_ID);
            assertThat(userResponse.username()).isEqualTo(TEST_USERNAME);
            assertThat(userResponse.email()).isEqualTo(TEST_EMAIL);
        }

        @Test
        @DisplayName("이메일로 유저 조회 성공")
        void getUserByEmail_Success() {
            // when
            UserResponse userResponse = userService.getUserByEmail(TEST_EMAIL);

            // then
            assertThat(userResponse).isNotNull();
            assertThat(userResponse.id()).isEqualTo(TEST_USER_ID);
            assertThat(userResponse.username()).isEqualTo(TEST_USERNAME);
            assertThat(userResponse.email()).isEqualTo(TEST_EMAIL);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 유저 조회 실패")
        void getUser_Fail_UserNotFound() {
            // when & then
            assertThrows(Exception.class, () -> 
                userService.getUser("nonexistentuser")
            );
        }
    }

    @Nested
    @DisplayName("유저 정보 수정 테스트")
    class UserUpdateTest {
        
        @BeforeEach
        void createTestUser() {
            userService.createUser(testUserRequest);
        }
        
        @Test
        @DisplayName("유저 정보 수정 성공")
        void updateUser_Success() {
            // given
            String newUsername = "Updated User";
            String newEmail = "updated@example.com";

            UserRequest updateRequest = UserRequest.builder()
                    .id(TEST_USER_ID)
                    .username(newUsername)
                    .email(newEmail)
                    .build();

            // when
            boolean result = userService.updateUser(updateRequest);

            // then
            assertThat(result).isTrue();
            
            User updatedUser = userRepository.findById(TEST_USER_ID)
                    .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));
            
            assertThat(updatedUser.getUsername()).isEqualTo(newUsername);
            assertThat(updatedUser.getEmail()).isEqualTo(newEmail);
        }

        @Test
        @DisplayName("존재하지 않는 유저 수정 실패")
        void updateUser_Fail_UserNotFound() {
            // given
            UserRequest updateRequest = UserRequest.builder()
                    .id("nonexistentuser")
                    .username("Updated User")
                    .email("updated@example.com")
                    .build();

            // when & then
            assertThrows(Exception.class, () -> 
                userService.updateUser(updateRequest)
            );
        }
    }

    @Nested
    @DisplayName("유저 삭제 테스트")
    class UserDeletionTest {
        
        @BeforeEach
        void createTestUser() {
            userService.createUser(testUserRequest);
        }
        
        @Test
        @DisplayName("유저 삭제 성공")
        void deleteUser_Success() {
            // when
            boolean result = userService.deleteUser(TEST_USER_ID);

            // then
            assertThat(result).isTrue();
            assertThat(userRepository.findById(TEST_USER_ID)).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 유저 삭제 실패")
        void deleteUser_Fail_UserNotFound() {
            // when & then
            assertThrows(Exception.class, () -> 
                userService.deleteUser("nonexistentuser")
            );
        }
    }
} 