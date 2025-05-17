package org.example.plain.domain.chat.integration;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.plain.common.enums.Role;
import org.example.plain.domain.chat.dto.ChatMessageDTO;
import org.example.plain.domain.chat.dto.ChatRoomDTO;
import org.example.plain.domain.chat.entity.ChatStatus;
import org.example.plain.domain.classLecture.entity.ClassLecture;
import org.example.plain.domain.classLecture.repository.ClassLectureRepository;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ChatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassLectureRepository classLectureRepository;

    private User testUser;
    private ClassLecture testLecture;
    private String testChatId;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .id("testUser")
                .username("Test User")
                .role(Role.TEACHER)
                .email("test@example.com")
                .password("password")
                .build();
        userRepository.save(testUser);

        // 테스트 강의 생성
        testLecture = ClassLecture.builder()
                .id("testLecture")
                .title("Test Lecture")
                .description("Test Description")
                .instructor(testUser)
                .build();
        testLecture = classLectureRepository.save(testLecture);
    }

    @Test
    @WithMockUser(username = "testUser")
    void createAndJoinChatRoom() throws Exception {
        // 채팅방 생성
        MvcResult createResult = mockMvc.perform(post("/api/v1/chat/rooms")
                        .param("lectureId", testLecture.getId())
                        .param("chatName", "Test Chat Room")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        ChatRoomDTO chatRoom = parseResponse(createResult, ChatRoomDTO.class);
        assertThat(chatRoom).isNotNull();
        assertThat(chatRoom.getChatName()).isEqualTo("Test Chat Room");
        testChatId = chatRoom.getChatId();

        // 채팅방 참여
        mockMvc.perform(post("/api/v1/chat/rooms/{chatId}/join", testChatId))
                .andExpect(status().isOk());

        // 채팅방 정보 조회
        MvcResult getResult = mockMvc.perform(get("/api/v1/chat/rooms/{chatId}", testChatId))
                .andExpect(status().isOk())
                .andReturn();

        ChatRoomDTO retrievedRoom = parseResponse(getResult, ChatRoomDTO.class);
        assertThat(retrievedRoom).isNotNull();
        assertThat(retrievedRoom.getChatId()).isEqualTo(testChatId);
    }

    @Test
    @WithMockUser(username = "testUser")
    void sendAndReceiveMessage() throws Exception {
        // 채팅방 생성 및 참여
        MvcResult createResult = mockMvc.perform(post("/api/v1/chat/rooms")
                        .param("lectureId", testLecture.getId())
                        .param("chatName", "Test Chat Room")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        ChatRoomDTO chatRoom = parseResponse(createResult, ChatRoomDTO.class);
        testChatId = chatRoom.getChatId();

        mockMvc.perform(post("/api/v1/chat/rooms/{chatId}/join", testChatId))
                .andExpect(status().isOk());

        // 메시지 전송
        MvcResult sendResult = mockMvc.perform(get("/api/v1/chat/rooms/{chatId}/messages", testChatId)
                        .param("content", "Hello, World!")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        ChatMessageDTO sentMessage = parseResponse(sendResult, ChatMessageDTO.class);
        assertThat(sentMessage).isNotNull();
        assertThat(sentMessage.getContent()).isEqualTo("Hello, World!");

        // 메시지 조회
        MvcResult getResult = mockMvc.perform(get("/api/v1/chat/rooms/{chatId}/messages", testChatId))
                .andExpect(status().isOk())
                .andReturn();

        List<ChatMessageDTO> messages = parseResponseList(getResult, ChatMessageDTO.class);
        assertThat(messages).isNotEmpty();
        assertThat(messages.get(0).getContent()).isEqualTo("Hello, World!");
    }

    @Test
    @WithMockUser(username = "testUser")
    void leaveChatRoom() throws Exception {
        // 채팅방 생성 및 참여
        MvcResult createResult = mockMvc.perform(post("/api/v1/chat/rooms")
                        .param("lectureId", testLecture.getId())
                        .param("chatName", "Test Chat Room")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        ChatRoomDTO chatRoom = parseResponse(createResult, ChatRoomDTO.class);
        testChatId = chatRoom.getChatId();

        mockMvc.perform(post("/api/v1/chat/rooms/{chatId}/join", testChatId))
                .andExpect(status().isOk());

        // 채팅방 나가기
        mockMvc.perform(post("/api/v1/chat/rooms/{chatId}/leave", testChatId))
                .andExpect(status().isOk());

        // 나간 후 메시지 전송 시도 시 실패
        mockMvc.perform(post("/api/v1/chat/rooms/{chatId}/messages", testChatId)
                        .param("content", "This should fail")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    private <T> T parseResponse(MvcResult result, Class<T> responseType) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper.readValue(result.getResponse().getContentAsString(), responseType);
    }

    private <T> List<T> parseResponseList(MvcResult result, Class<T> elementType) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, elementType);
        return mapper.readValue(result.getResponse().getContentAsString(), type);
    }
} 