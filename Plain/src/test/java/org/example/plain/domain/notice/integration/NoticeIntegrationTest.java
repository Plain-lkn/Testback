package org.example.plain.domain.notice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.plain.common.enums.Role;
import org.example.plain.domain.classLecture.entity.ClassLecture;
import org.example.plain.domain.classLecture.repository.ClassLectureRepositoryPort;
import org.example.plain.domain.notice.dto.NoticeRequest;
import org.example.plain.domain.notice.dto.NoticeUpdateRequest;
import org.example.plain.domain.notice.entity.NoticeEntity;
import org.example.plain.domain.notice.repository.NoticeRepository;
import org.example.plain.domain.user.dto.CustomUserDetails;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NoticeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassLectureRepositoryPort classLectureRepositoryPort;

    private User testUser;
    private ClassLecture testClassLecture;
    private NoticeEntity testNotice;
    private NoticeRequest noticeRequest;
    private NoticeUpdateRequest noticeUpdateRequest;

    @BeforeEach
    void setUp() throws Exception {
        // 사용자 생성 및 저장
        testUser = User.builder()
                .id("test-user-id")
                .username("testuser")
                .email("test@example.com")
                .role(Role.NORMAL)
                .password("password")
                .build();
        userRepository.save(testUser);

        // 테스트용 사용자 인증 설정
        setupTestAuthentication(testUser);

        // ClassLecture 생성 및 저장
        testClassLecture = ClassLecture.builder()
                .id("class-lecture-id")
                .title("Test Class")
                .description("Test Class Description")
                .code("TEST123")
                .instructor(testUser)
                .maxMember(30L)
                .build();
        ClassLecture savedClassLecture = classLectureRepositoryPort.save(testClassLecture);

        // 테스트 공지사항 생성 및 저장
        testNotice = NoticeEntity.create(
                "Test Notice Title", 
                "Test Notice Content", 
                testUser, 
                savedClassLecture
        );
        testNotice = noticeRepository.save(testNotice); // 저장된 객체 참조 업데이트
        
        // 요청 DTO 생성
        noticeRequest = NoticeRequest.builder()
                .title("New Test Notice")
                .content("New Test Content")
                .c_id(savedClassLecture.getId())
                .build();
        
        // 이제 저장된 엔티티에서 ID를 가져와서 사용
        noticeUpdateRequest = NoticeUpdateRequest.builder()
                .noticeId(testNotice.getNoticeId())
                .title("Updated Title")
                .content("Updated Content")
                .build();
    }
    
    private void setupTestAuthentication(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @Test
    @DisplayName("공지사항 생성 통합 테스트")
    void createNotice() throws Exception {
        // Given - setUp 메서드에서 설정됨

        // When & Then
        mockMvc.perform(post("/notice")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noticeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("공지사항 목록 조회 통합 테스트")
    void getAllNotice() throws Exception {
        // Given - setUp 메서드에서 이미 공지사항 생성됨

        // When & Then
        mockMvc.perform(get("/notice")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title", is(testNotice.getTitle())));
    }

    @Test
    @DisplayName("공지사항 상세 조회 통합 테스트")
    void getNotice() throws Exception {
        // Given - setUp 메서드에서 이미 공지사항 생성됨

        // When & Then
        mockMvc.perform(get("/notice/{notice_id}", testNotice.getNoticeId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.title").value(testNotice.getTitle()))
                .andExpect(jsonPath("$.data.content").value(testNotice.getContent()));
    }

    @Test
    @DisplayName("공지사항 수정 통합 테스트")
    void updateNotice() throws Exception {
        // Given - setUp 메서드에서 이미 updateRequest 생성됨

        // When & Then
        mockMvc.perform(patch("/notice/{notice_id}", testNotice.getNoticeId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noticeUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("공지사항 삭제 통합 테스트")
    void deleteNotice() throws Exception {
        // Given - setUp 메서드에서 이미 공지사항 생성됨

        // When & Then
        mockMvc.perform(delete("/notice/{notice_id}", testNotice.getNoticeId())
                .with(csrf()))
                .andExpect(status().isOk());
        
        // 삭제 확인
        mockMvc.perform(get("/notice/{notice_id}", testNotice.getNoticeId())
                .with(csrf()))
                .andExpect(status().is4xxClientError()); // 없는 공지사항 조회 시 에러
    }
} 