package org.example.plain.domain.notice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.plain.common.ResponseField;
import org.example.plain.common.config.SecurityUtils;
import org.example.plain.common.enums.Message;
import org.example.plain.common.enums.Role;
import org.example.plain.domain.classLecture.entity.ClassLecture;
import org.example.plain.domain.notice.dto.*;
import org.example.plain.domain.notice.service.NoticeService;
import org.example.plain.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoticeController.class)
class NoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NoticeService noticeService;

    @MockBean
    private SecurityUtils securityUtils;

    private NoticeRequest noticeRequest;
    private NoticeUpdateRequest noticeUpdateRequest;
    private NoticeResponse noticeResponse;
    private User testUser;
    private ClassLecture testClassLecture;
    private String testNoticeId = "test1";

    @BeforeEach
    void setUp() throws Exception {
        // 테스트 유저 설정
        testUser = User.builder()
                .id("test-user-id")
                .username("testuser")
                .email("test@example.com")
                .role(Role.NORMAL)
                .build();
        
        // 테스트 ClassLecture 설정
        testClassLecture = ClassLecture.builder()
                .id("class-lecture-id")
                .title("Test Class")
                .description("Test Class Description")
                .code("TEST123")
                .instructor(testUser)
                .maxMember(30L)
                .build();
        
        // 테스트 요청 데이터 설정 - 리플렉션을 사용하여 필드 설정
        noticeRequest = new NoticeRequest();
        setField(noticeRequest, "title", "Test Notice");
        setField(noticeRequest, "content", "Test Content");
        setField(noticeRequest, "c_id", "class-lecture-id");
        
        noticeUpdateRequest = new NoticeUpdateRequest();
        setField(noticeUpdateRequest, "noticeId", testNoticeId);
        setField(noticeUpdateRequest, "title", "Updated Notice");
        setField(noticeUpdateRequest, "content", "Updated Content");
        
        noticeResponse = NoticeResponse.builder()
                .noticeId(testNoticeId)
                .title("Test Notice")
                .content("Test Content")
                .user(testUser)
                .c_id("class-lecture-id")
                .createDate(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
    }
    
    // 리플렉션을 사용하여 private 필드 설정하는 헬퍼 메서드
    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @Test
    @DisplayName("공지사항 생성 테스트")
    @WithMockUser
    void createNotice() throws Exception {
        // Given
        ResponseField<NoticeResponse> responseField = new ResponseField<>(Message.OK.name(), HttpStatus.OK, noticeResponse);
        when(noticeService.createNotice(any(NoticeRequest.class), anyString())).thenReturn(responseField);
        when(securityUtils.getUserId()).thenReturn("test-user-id");
        
        // When & Then
        mockMvc.perform(post("/notice")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noticeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value(Message.OK.name()));
        
        verify(noticeService, times(1)).createNotice(any(NoticeRequest.class), anyString());
    }

    @Test
    @DisplayName("공지사항 수정 테스트")
    @WithMockUser
    void updateNotice() throws Exception {
        // Given
        ResponseField<NoticeResponse> responseField = new ResponseField<>(Message.OK.name(), HttpStatus.OK, noticeResponse);
        when(noticeService.updateNotice(any(NoticeUpdateRequest.class), anyString())).thenReturn(responseField);
        when(securityUtils.getUserId()).thenReturn("test-user-id");
        
        // When & Then
        mockMvc.perform(patch("/notice/{notice_id}", testNoticeId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noticeUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value(Message.OK.name()));
        
        verify(noticeService, times(1)).updateNotice(any(NoticeUpdateRequest.class), anyString());
    }

    @Test
    @DisplayName("공지사항 목록 조회 테스트")
    @WithMockUser
    void getAllNotice() throws Exception {
        // Given
        List<NoticeResponse> noticeResponses = Arrays.asList(noticeResponse);
        ResponseField<List<NoticeResponse>> responseField = new ResponseField<>(Message.OK.name(), HttpStatus.OK, noticeResponses);
        when(noticeService.getAllNotice()).thenReturn(responseField);
        
        // When & Then
        mockMvc.perform(get("/notice")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value(Message.OK.name()))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].noticeId").value(testNoticeId));
        
        verify(noticeService, times(1)).getAllNotice();
    }

    @Test
    @DisplayName("공지사항 상세 조회 테스트")
    @WithMockUser
    void getNotice() throws Exception {
        // Given
        ResponseField<NoticeResponse> responseField = new ResponseField<>(Message.OK.name(), HttpStatus.OK, noticeResponse);
        when(noticeService.getNotice(testNoticeId)).thenReturn(responseField);
        
        // When & Then
        mockMvc.perform(get("/notice/{notice_id}", testNoticeId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value(Message.OK.name()))
                .andExpect(jsonPath("$.data.noticeId").value(testNoticeId));
        
        verify(noticeService, times(1)).getNotice(testNoticeId);
    }

    @Test
    @DisplayName("공지사항 삭제 테스트")
    @WithMockUser
    void deleteNotice() throws Exception {
        // Given
        doNothing().when(noticeService).deleteNotice(eq(testNoticeId), anyString());
        when(securityUtils.getUserId()).thenReturn("test-user-id");
        
        // When & Then
        mockMvc.perform(delete("/notice/{notice_id}", testNoticeId)
                .with(csrf()))
                .andExpect(status().isOk());
        
        verify(noticeService, times(1)).deleteNotice(eq(testNoticeId), anyString());
    }
} 