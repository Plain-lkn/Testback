package org.example.plain.domain.meeting.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.plain.common.config.SecurityUtils;
import org.example.plain.domain.meeting.dto.ChatMessage;
import org.example.plain.domain.meeting.dto.ParticipantState;
import org.example.plain.domain.meeting.dto.SignalMessage;
import org.example.plain.domain.meeting.service.MeetingService;
import org.example.plain.domain.meeting.service.SignalingService;
import org.springframework.data.redis.core.RedisTemplate;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingWebSocketHandler extends TextWebSocketHandler {
    private final MeetingService meetingService;
    private final SignalingService signalingService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        String username = (String) session.getAttributes().get("username");
        String roomId = extractRoomId(session);
        
        // 방에 세션 추가
        roomSessions.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(session);
        
        log.info("WebSocket connection established - roomId: {}, userId: {}, username: {}", 
                roomId, userId, username);
        
        meetingService.handleParticipantJoin(roomId, userId, username);
        
        // 입장 메시지 전송
        Map<String, String> message = Map.of(
            "type", "system",
            "message", username + "님이 입장하셨습니다."
        );
        broadcastToRoom(roomId, new TextMessage(objectMapper.writeValueAsString(message)));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        String username = (String) session.getAttributes().get("username");
        String roomId = extractRoomId(session);
        
        log.info("Received message from roomId: {}, userId: {}, username: {}, message: {}", 
                roomId, userId, username, message.getPayload());
        
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String type = (String) payload.get("type");
        
        switch (type) {
            case "offer":
                handleOfferSignal(roomId, payload);
                break;
            case "answer":
                handleAnswerSignal(roomId, payload);
                break;
            case "candidate":
                handleCandidateSignal(roomId, payload);
                break;
            case "chat":
                handleChatMessage(roomId, userId, username, payload);
                break;
            case "state":
                handleStateChange(roomId, userId, username, payload);
                break;
            default:
                // 알 수 없는 메시지 타입은 에코
                broadcastToRoom(roomId, message);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        String username = (String) session.getAttributes().get("username");
        String roomId = extractRoomId(session);
        
        // 방에서 세션 제거
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(roomId);
            }
        }
        
        log.info("WebSocket connection closed - roomId: {}, userId: {}, username: {}, status: {}", 
                roomId, userId, username, status);
        
        meetingService.handleParticipantLeave(roomId, userId);
        
        // 퇴장 메시지 전송
        Map<String, String> message = Map.of(
            "type", "system",
            "message", username + "님이 퇴장하셨습니다."
        );
        broadcastToRoom(roomId, new TextMessage(objectMapper.writeValueAsString(message)));
    }

    private String extractRoomId(WebSocketSession session) {
        String path = session.getUri().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private synchronized void broadcastToRoom(String roomId, TextMessage message) throws Exception {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(message);
                    } catch (IOException e) {
                        log.error("Failed to send message to session: {}", e.getMessage());
                    }
                }
            }
        }
    }

    private void handleOfferSignal(String roomId, Map<String, Object> payload) throws Exception {
        String offerSdp = (String) payload.get("data");
        redisTemplate.opsForValue().set("meeting:room:" + roomId + ":offer", offerSdp);
        broadcastToRoom(roomId,new TextMessage(objectMapper.writeValueAsString(payload)));
    }

    private void handleAnswerSignal(String roomId, Map<String, Object> payload) throws Exception {
        String answerSdp = (String) payload.get("data");
        redisTemplate.opsForValue().set("meeting:room:" + roomId + ":answer", answerSdp);
        broadcastToRoom(roomId,new TextMessage(objectMapper.writeValueAsString(payload)));
    }

    private void handleCandidateSignal(String roomId, Map<String, Object> payload) throws Exception {
        String candidate = (String) payload.get("data");
        redisTemplate.opsForList().rightPush("meeting:room:" + roomId + ":candidates", candidate);
        broadcastToRoom(roomId,new TextMessage(objectMapper.writeValueAsString(payload)));
    }

    private void handleChatMessage(String roomId, String userId, String username, Map<String, Object> payload) throws Exception {
        String content = (String) payload.get("content");
        
        // 채팅 메시지를 모든 참가자에게 브로드캐스트
        Map<String, Object> chatMessage = Map.of(
            "type", "chat",
            "userId", userId,
            "userName", username,
            "content", content
        );
        broadcastToRoom(roomId, new TextMessage(objectMapper.writeValueAsString(chatMessage)));
    }

    private void handleStateChange(String roomId, String userId, String username, Map<String, Object> payload) throws Exception {
        boolean isMuted = (boolean) payload.get("isMuted");
        boolean isVideoOff = (boolean) payload.get("isVideoOff");
        
        // 상태 변경을 모든 참가자에게 브로드캐스트
        Map<String, Object> stateMessage = Map.of(
            "type", "state",
            "userId", userId,
            "isMuted", isMuted,
            "isVideoOff", isVideoOff
        );
        broadcastToRoom(roomId, new TextMessage(objectMapper.writeValueAsString(stateMessage)));
    }
} 