package org.example.plain.domain.chat.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.plain.domain.chat.dto.ChatMessageRequest;
import org.example.plain.domain.chat.dto.ChatMessageResponse;
import org.example.plain.domain.chat.service.ClassChatService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final ClassChatService classChatService;
    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String chatId = extractChatId(session);
        sessions.put(session.getId(), session);
        log.info("New WebSocket connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String chatId = extractChatId(session);
        String payload = message.getPayload();
        log.info("Received message: {}", payload);

        ChatMessageRequest chatMessage = objectMapper.readValue(payload, ChatMessageRequest.class);
        chatMessage.setTimestamp(LocalDateTime.now());

        switch (chatMessage.getType()) {
            case ENTER -> handleEnterMessage(chatId, chatMessage);
            case LEAVE -> handleLeaveMessage(chatId, chatMessage);
            case TALK -> handleTalkMessage(chatId, chatMessage);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String chatId = extractChatId(session);
        sessions.remove(session.getId());
        log.info("WebSocket connection closed: {}", session.getId());
    }

    private String extractChatId(WebSocketSession session) {
        String uri = session.getUri().toString();
        return uri.substring(uri.lastIndexOf("/") + 1);
    }

    private void handleEnterMessage(String chatId, ChatMessageRequest message) {
        classChatService.joinChatRoom(chatId, message.getSenderId());
        String welcomeMessage = String.format("%s님이 입장하셨습니다.", message.getSenderName());
        broadcastMessage(chatId, ChatMessageResponse.builder()
                .type(ChatMessageRequest.MessageType.TALK)
                .senderId("SYSTEM")
                .content(welcomeMessage)
                .timestamp(LocalDateTime.now())
                .build());
    }

    private void handleLeaveMessage(String chatId, ChatMessageRequest message) {
        classChatService.leaveChatRoom(chatId, message.getSenderId());
        String goodbyeMessage = String.format("%s님이 퇴장하셨습니다.", message.getSenderName());
        broadcastMessage(chatId, ChatMessageResponse.builder()
                .type(ChatMessageRequest.MessageType.TALK)
                .senderId("SYSTEM")
                .content(goodbyeMessage)
                .timestamp(LocalDateTime.now())
                .build());
    }

    private void handleTalkMessage(String chatId, ChatMessageRequest message) {
        // DB에 메시지 저장
        classChatService.sendMessage(chatId, message.getSenderId(), message.getContent());

        // 메시지 브로드캐스트
        broadcastMessage(chatId, ChatMessageResponse.builder()
                .type(message.getType())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .timestamp(LocalDateTime.now())
                .build());
    }

    private void broadcastMessage(String chatId, ChatMessageResponse message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            sessions.values().stream()
                    .filter(session -> extractChatId(session).equals(chatId))
                    .forEach(session -> {
                        try {
                            session.sendMessage(new TextMessage(jsonMessage));
                        } catch (Exception e) {
                            log.error("Error sending message to session {}: {}", session.getId(), e.getMessage());
                        }
                    });
        } catch (Exception e) {
            log.error("Error broadcasting message: {}", e.getMessage());
        }
    }
} 