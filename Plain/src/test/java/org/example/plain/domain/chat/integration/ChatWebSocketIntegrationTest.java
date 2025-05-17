package org.example.plain.domain.chat.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.plain.common.enums.Role;
import org.example.plain.domain.chat.dto.ChatMessageRequest;
import org.example.plain.domain.chat.dto.ChatMessageResponse;
import org.example.plain.domain.chat.dto.ChatRoomDTO;
import org.example.plain.domain.chat.service.ClassChatService;
import org.example.plain.domain.classLecture.entity.ClassLecture;
import org.example.plain.domain.classLecture.repository.ClassLectureRepository;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.repository.UserRepository;
import org.example.plain.domain.user.service.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatWebSocketIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketIntegrationTest.class);

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassLectureRepository classLectureRepository;

    @Autowired
    private ClassChatService chatService;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private BlockingQueue<ChatMessageResponse> messages;
    private String chatId;
    private User testUser;
    private ClassLecture testLecture;

    @BeforeEach
    void setUp() {
        messages = new LinkedBlockingQueue<>();

        // 테스트 데이터 설정
        testUser = User.builder()
                .id("testUser")
                .username("Test User")
                .role(Role.NORMAL)
                .email("test@example.com")
                .password("password")
                .build();
        userRepository.save(testUser);

        testLecture = ClassLecture.builder()
                .id("testLecture")
                .title("Test Lecture")
                .description("Test Description")
                .instructor(testUser)
                .build();
        testLecture = classLectureRepository.save(testLecture);

        // 채팅방 생성
        ChatRoomDTO chatRoom = chatService.createChatRoom(testLecture.getId(), "Test Chat Room");
        chatId = chatRoom.getChatId();
    }

    @Test
    void testWebSocketChatCommunication() throws Exception {
        String url = "ws://localhost:" + port + "/ws/chat/" + chatId;
        
        // WebSocket 연결 시 인증 헤더 설정
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        String token = jwtUtil.makeJwtToken(testUser.getId(), testUser.getUsername());
        headers.add(HttpHeaders.AUTHORIZATION, token);

        // WebSocket 클라이언트 설정
        StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
        org.springframework.web.socket.WebSocketSession session = webSocketClient.doHandshake(
            new TextWebSocketHandler() {
                @Override
                public void handleTextMessage(org.springframework.web.socket.WebSocketSession session, TextMessage message) {
                    try {
                        ChatMessageResponse response = objectMapper.readValue(message.getPayload(), ChatMessageResponse.class);
                        logger.info("Received message: {}", response);
                        messages.add(response);
                    } catch (Exception e) {
                        logger.error("Error handling message: {}", e.getMessage());
                    }
                }

                @Override
                public void afterConnectionEstablished(org.springframework.web.socket.WebSocketSession session) {
                    logger.info("WebSocket connection established");
                }

                @Override
                public void handleTransportError(org.springframework.web.socket.WebSocketSession session, Throwable exception) {
                    logger.error("Transport error: {}", exception.getMessage());
                }

                @Override
                public void afterConnectionClosed(org.springframework.web.socket.WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
                    logger.info("WebSocket connection closed: {}", status);
                }
            },
            url,
            headers
        ).get(5, TimeUnit.SECONDS);

        // 입장 메시지 전송
        ChatMessageRequest enterMessage = ChatMessageRequest.builder()
                .type(ChatMessageRequest.MessageType.ENTER)
                .chatId(chatId)
                .senderId(testUser.getId())
                .senderName(testUser.getUsername())
                .build();
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(enterMessage)));

        // 채팅 메시지 전송
        ChatMessageRequest chatMessage = ChatMessageRequest.builder()
                .type(ChatMessageRequest.MessageType.TALK)
                .chatId(chatId)
                .senderId(testUser.getId())
                .content("Hello, WebSocket!")
                .senderName(testUser.getUsername())
                .build();
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));

        // 메시지 수신 확인
        ChatMessageResponse receivedEnterMessage = messages.poll(5, TimeUnit.SECONDS);
        assertThat(receivedEnterMessage).isNotNull();
        assertThat(receivedEnterMessage.getType()).isEqualTo(ChatMessageRequest.MessageType.TALK);
        assertThat(receivedEnterMessage.getSenderId()).isEqualTo("SYSTEM");

        ChatMessageResponse receivedChatMessage = messages.poll(5, TimeUnit.SECONDS);
        assertThat(receivedChatMessage).isNotNull();
        assertThat(receivedChatMessage.getType()).isEqualTo(ChatMessageRequest.MessageType.TALK);
        assertThat(receivedChatMessage.getContent()).isEqualTo("Hello, WebSocket!");

        // 퇴장 메시지 전송
        ChatMessageRequest leaveMessage = ChatMessageRequest.builder()
                .type(ChatMessageRequest.MessageType.LEAVE)
                .chatId(chatId)
                .senderId(testUser.getId())
                .senderName(testUser.getUsername())
                .build();
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(leaveMessage)));

        ChatMessageResponse receivedLeaveMessage = messages.poll(5, TimeUnit.SECONDS);
        assertThat(receivedLeaveMessage).isNotNull();
        assertThat(receivedLeaveMessage.getType()).isEqualTo(ChatMessageRequest.MessageType.TALK);
        assertThat(receivedLeaveMessage.getSenderId()).isEqualTo("SYSTEM");

        session.close();
    }
} 