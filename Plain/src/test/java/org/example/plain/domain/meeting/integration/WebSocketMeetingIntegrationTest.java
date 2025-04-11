package org.example.plain.domain.meeting.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.plain.domain.user.service.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WebSocketMeetingIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private String userId;
    private String username;
    private String token;
    private String roomId;

    @BeforeEach
    void setUp() {
        baseUrl = "ws://localhost:" + port;
        userId = UUID.randomUUID().toString();
        username = "testUser";
        token = jwtUtil.makeJwtToken(userId, username);
        roomId = "testRoom";
    }

    @Test
    void testWebSocketConnectionWithValidToken() throws Exception {
        StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
        CompletableFuture<TextMessage> messageFuture = new CompletableFuture<>();
        CompletableFuture<Boolean> connectionFuture = new CompletableFuture<>();

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("Authorization", token);

        WebSocketSession session = webSocketClient.execute(
            new TestWebSocketHandler(messageFuture, connectionFuture),
            headers,
            URI.create(baseUrl + "/ws/meeting/" + roomId)
        ).get(5, TimeUnit.SECONDS);

        assertTrue(connectionFuture.get(5, TimeUnit.SECONDS));
        assertTrue(session.isOpen());

        // WebRTC offer 시그널링 테스트
        Map<String, Object> offerMessage = new HashMap<>();
        offerMessage.put("type", "offer");
        offerMessage.put("targetUserId", "targetUser");
        offerMessage.put("data", "testOfferData");
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(offerMessage)));

        // 채팅 메시지 테스트
        Map<String, Object> chatMessage = new HashMap<>();
        chatMessage.put("type", "chat");
        chatMessage.put("content", "Hello, World!");
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));

        // 상태 변경 테스트
        Map<String, Object> stateMessage = new HashMap<>();
        stateMessage.put("type", "state");
        stateMessage.put("isMuted", true);
        stateMessage.put("isVideoOff", false);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(stateMessage)));

        // 메시지 수신 확인
        TextMessage receivedMessage = messageFuture.get(5, TimeUnit.SECONDS);
        assertNotNull(receivedMessage);

        session.close();
    }

    @Test
    void testWebSocketConnectionWithInvalidToken() {
        StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
        CompletableFuture<TextMessage> messageFuture = new CompletableFuture<>();
        CompletableFuture<Boolean> connectionFuture = new CompletableFuture<>();

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("Authorization", "Bearer invalidToken");

        assertThrows(Exception.class, () -> {
            webSocketClient.execute(
                new TestWebSocketHandler(messageFuture, connectionFuture),
                headers,
                URI.create(baseUrl + "/ws/meeting/" + roomId)
            ).get(5, TimeUnit.SECONDS);
        });
    }

    @Test
    void testWebSocketConnectionWithoutToken() {
        StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
        CompletableFuture<TextMessage> messageFuture = new CompletableFuture<>();
        CompletableFuture<Boolean> connectionFuture = new CompletableFuture<>();

        assertThrows(Exception.class, () -> {
            webSocketClient.execute(
                new TestWebSocketHandler(messageFuture, connectionFuture),
                new WebSocketHttpHeaders(),
                URI.create(baseUrl + "/ws/meeting/" + roomId)
            ).get(5, TimeUnit.SECONDS);
        });
    }

    private static class TestWebSocketHandler extends TextWebSocketHandler {
        private final CompletableFuture<TextMessage> messageFuture;
        private final CompletableFuture<Boolean> connectionFuture;

        public TestWebSocketHandler(CompletableFuture<TextMessage> messageFuture, CompletableFuture<Boolean> connectionFuture) {
            this.messageFuture = messageFuture;
            this.connectionFuture = connectionFuture;
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            connectionFuture.complete(true);
        }

        @Override
        public void handleTextMessage(WebSocketSession session, TextMessage message) {
            messageFuture.complete(message);
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) {
            connectionFuture.completeExceptionally(exception);
            messageFuture.completeExceptionally(exception);
        }
    }
} 