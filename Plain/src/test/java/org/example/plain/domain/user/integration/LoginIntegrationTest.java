package org.example.plain.domain.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.plain.domain.user.dto.UserLoginRequest;
import org.example.plain.domain.user.dto.UserRequest;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.interfaces.UserService;
import org.example.plain.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String TEST_USER_ID = "testUser";
    private final String TEST_PASSWORD = "testPassword123!";
    private final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class SignUp {
        @Test
        @DisplayName("정상적인 회원가입")
        void successfulSignUp() throws Exception {
            // given
            UserRequest userRequest = new UserRequest(TEST_USER_ID, "TestUser", TEST_EMAIL, TEST_PASSWORD);
            String content = objectMapper.writeValueAsString(userRequest);

            // when
            ResultActions result = mockMvc.perform(post("/account/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content));

            // then
            result.andExpect(status().isNoContent());
            User savedUser = userRepository.findById(TEST_USER_ID).orElseThrow();
            assertThat(savedUser.getId()).isEqualTo(TEST_USER_ID);
            assertThat(savedUser.getEmail()).isEqualTo(TEST_EMAIL);
        }

        @Test
        @DisplayName("중복된 아이디로 회원가입 시도")
        void duplicateIdSignUp() throws Exception {
            // given
            UserRequest existingUser = new UserRequest(TEST_USER_ID, "ExistingUser", "existing@example.com", "password123!");
            userService.createUser(existingUser);

            UserRequest duplicateUser = new UserRequest(TEST_USER_ID, "DuplicateUser", "duplicate@example.com", "password456!");
            String content = objectMapper.writeValueAsString(duplicateUser);

            // when
            ResultActions result = mockMvc.perform(post("/account/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content));

            // then
            result.andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class Login {
        @BeforeEach
        void setUp() {
            UserRequest userRequest = new UserRequest(TEST_USER_ID, "TestUser", TEST_EMAIL, TEST_PASSWORD);
            userService.createUser(userRequest);
        }

        @Test
        @DisplayName("정상적인 로그인")
        void successfulLogin() throws Exception {
            // given
            UserLoginRequest loginRequest = new UserLoginRequest(TEST_USER_ID, TEST_PASSWORD);
            String content = objectMapper.writeValueAsString(loginRequest);

            // when
            ResultActions result = mockMvc.perform(post("/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content));

            // then
            result.andExpect(status().isOk())
                    .andExpect(header().exists("Authorization"))
                    .andExpect(cookie().exists("token"));
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인")
        void loginWithWrongPassword() throws Exception {
            // given
            UserLoginRequest loginRequest = new UserLoginRequest(TEST_USER_ID, "wrongPassword");
            String content = objectMapper.writeValueAsString(loginRequest);

            // when
            ResultActions result = mockMvc.perform(post("/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content));

            // then
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 로그인")
        void loginWithNonExistentUser() throws Exception {
            // given
            UserLoginRequest loginRequest = new UserLoginRequest("nonexistent", TEST_PASSWORD);
            String content = objectMapper.writeValueAsString(loginRequest);

            // when
            ResultActions result = mockMvc.perform(post("/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content));

            // then
            result.andExpect(status().isUnauthorized());
        }
    }
} 