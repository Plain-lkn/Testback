package org.example.plain.domain.meeting.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.plain.common.config.SecurityUtils;
import org.example.plain.domain.meeting.dto.ChatMessage;
import org.example.plain.domain.meeting.dto.ParticipantState;
import org.example.plain.domain.meeting.dto.SignalMessage;
import org.example.plain.domain.meeting.service.MeetingService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MeetingWebSocketHandler extends TextWebSocketHandler {
    private final MeetingService meetingService;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = extractRoomId(session);
        String userId = SecurityUtils.getUserId();
        String userName = extractUserName(session);
        meetingService.handleParticipantJoin(roomId, userId, userName);
        
        // 다른 참가자들에게 새 참가자 입장 알림
        broadcastMessage(roomId, new TextMessage(
            objectMapper.writeValueAsString(Map.of(
                "type", "system",
                "message", userName + "님이 입장하셨습니다."
            ))
        ));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String roomId = extractRoomId(session);
        String userId = SecurityUtils.getUserId();
        String userName = extractUserName(session);
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String type = (String) payload.get("type");
        
        switch (type) {
            case "offer":
            case "answer":
            case "candidate":
                handleSignalMessage(roomId, userId, type, payload);
                break;
            case "chat":
                handleChatMessage(roomId, userId, userName, payload);
                break;
            case "state":
                handleStateChange(roomId, userId, userName, payload);
                break;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomId = extractRoomId(session);
        String userId = SecurityUtils.getUserId();
        String userName = extractUserName(session);
        meetingService.handleParticipantLeave(roomId, userId);
        
        // 다른 참가자들에게 참가자 퇴장 알림
        broadcastMessage(roomId, new TextMessage(
            objectMapper.writeValueAsString(Map.of(
                "type", "system",
                "message", userName + "님이 퇴장하셨습니다."
            ))
        ));
    }

    private void handleSignalMessage(String roomId, String userId, String signalType, Map<String, Object> payload) throws IOException {
        String targetUserId = (String) payload.get("targetUserId");
        String signalData = (String) payload.get("data");
        
        SignalMessage signalMessage = SignalMessage.builder()
                .roomId(roomId)
                .senderId(userId)
                .type(signalType)
                .data(signalData)
                .build();
        meetingService.handleSignal(signalMessage);
        
        // 시그널 메시지를 특정 참가자에게만 전송
        WebSocketSession targetSession = findSessionByUserId(roomId, targetUserId);
        if (targetSession != null) {
            targetSession.sendMessage(new TextMessage(
                objectMapper.writeValueAsString(Map.of(
                    "type", signalType,
                    "fromUserId", userId,
                    "data", signalData
                ))
            ));
        }
    }

    private void handleChatMessage(String roomId, String userId, String userName, Map<String, Object> payload) throws IOException {
        String content = (String) payload.get("content");
        ChatMessage chatMessage = ChatMessage.builder()
                .type("chat")
                .roomId(roomId)
                .senderId(userId)
                .senderName(userName)
                .message(content)
                .timestamp(LocalDateTime.now())
                .build();
        meetingService.saveChatMessage(chatMessage);
        
        // 채팅 메시지를 모든 참가자에게 브로드캐스트
        broadcastMessage(roomId, new TextMessage(
            objectMapper.writeValueAsString(Map.of(
                "type", "chat",
                "userId", userId,
                "userName", userName,
                "content", content
            ))
        ));
    }

    private void handleStateChange(String roomId, String userId, String userName, Map<String, Object> payload) throws IOException {
        boolean isMuted = (boolean) payload.get("isMuted");
        boolean isVideoOff = (boolean) payload.get("isVideoOff");
        
        meetingService.updateParticipantState(roomId, userId, userName, isMuted, isVideoOff);
        
        // 상태 변경을 모든 참가자에게 브로드캐스트
        broadcastMessage(roomId, new TextMessage(
            objectMapper.writeValueAsString(Map.of(
                "type", "state",
                "userId", userId,
                "isMuted", isMuted,
                "isVideoOff", isVideoOff
            ))
        ));
    }

    private String extractRoomId(WebSocketSession session) {
        String path = session.getUri().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private String extractUserName(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.contains("user-name=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("user-name=")) {
                    return param.substring("user-name=".length());
                }
            }
        }
        return SecurityUtils.getUserId(); // 기본값으로 userId 사용
    }

    private void broadcastMessage(String roomId, TextMessage message) throws IOException {
        for (WebSocketSession session : findSessionsByRoomId(roomId)) {
            if (session.isOpen()) {
                session.sendMessage(message);
            }
        }
    }

    private WebSocketSession findSessionByUserId(String roomId, String userId) {
        return findSessionsByRoomId(roomId).stream()
                .filter(session -> SecurityUtils.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    private List<WebSocketSession> findSessionsByRoomId(String roomId) {
        // 실제 구현에서는 세션 관리 로직 필요
        return new ArrayList<>();
    }
} 