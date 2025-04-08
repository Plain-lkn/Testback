package org.example.plain.domain.meeting.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.plain.common.enums.Role;
import org.example.plain.domain.meeting.dto.SignalMessage;
import org.example.plain.domain.meeting.handler.MeetingWebSocketHandler;
import org.example.plain.domain.meeting.service.MeetingService;
import org.example.plain.domain.meeting.service.SignalingService;
import org.example.plain.domain.user.dto.CustomUserDetails;
import org.example.plain.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class WebRtcSignalingIntegrationTest {

    @Autowired
    private MeetingWebSocketHandler webSocketHandler;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private SignalingService signalingService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String roomId;
    private String userId;
    private String targetUserId;
    private WebSocketSession mockSession;
    private WebSocketSession mockTargetSession;

    @BeforeEach
    void setUp() {
        roomId = UUID.randomUUID().toString();
        userId = "user1";
        targetUserId = "user2";
        
        // Create user1 and its authentication
        User user1 = User.builder()
                .id(userId)
                .username("User One")
                .role(Role.NORMAL)
                .build();
        CustomUserDetails userDetails1 = new CustomUserDetails(user1);
        Authentication auth1 = new UsernamePasswordAuthenticationToken(userDetails1, null, userDetails1.getAuthorities());
        mockSession = mock(WebSocketSession.class);
        when(mockSession.getUri()).thenReturn(URI.create("/ws/meeting/" + roomId));
        when(mockSession.getPrincipal()).thenReturn(auth1);
        
        // Create user2 and its authentication
        User user2 = User.builder()
                .id(targetUserId)
                .username("User Two")
                .role(Role.NORMAL)
                .build();
        CustomUserDetails userDetails2 = new CustomUserDetails(user2);
        Authentication auth2 = new UsernamePasswordAuthenticationToken(userDetails2, null, userDetails2.getAuthorities());
        mockTargetSession = mock(WebSocketSession.class);
        when(mockTargetSession.getUri()).thenReturn(URI.create("/ws/meeting/" + roomId));
        when(mockTargetSession.getPrincipal()).thenReturn(auth2);

        // Set authentication in SecurityContext for the current thread
        SecurityContextHolder.getContext().setAuthentication(auth1);
    }

    @Test
    public void testHandleOfferSignal() throws Exception {
        // given
        String offerSdp = "v=0\r\no=- 123456789 2 IN IP4 127.0.0.1\r\ns=-\r\nt=0 0\r\n";
        Map<String, Object> payload = Map.of(
            "type", "offer",
            "targetUserId", targetUserId,
            "data", offerSdp
        );
        TextMessage message = new TextMessage(objectMapper.writeValueAsString(payload));

        // when
        webSocketHandler.handleTextMessage(mockSession, message);

        // then
        String storedOffer = redisTemplate.opsForValue().get("meeting:room:" + roomId + ":offer");
        assertThat(storedOffer).isEqualTo(offerSdp);
    }

    @Test
    public void testHandleAnswerSignal() throws Exception {
        // given
        String answerSdp = "v=0\r\no=- 987654321 2 IN IP4 127.0.0.1\r\ns=-\r\nt=0 0\r\n";
        Map<String, Object> payload = Map.of(
            "type", "answer",
            "targetUserId", targetUserId,
            "data", answerSdp
        );
        TextMessage message = new TextMessage(objectMapper.writeValueAsString(payload));

        // when
        webSocketHandler.handleTextMessage(mockSession, message);

        // then
        String storedAnswer = redisTemplate.opsForValue().get("meeting:room:" + roomId + ":answer");
        assertThat(storedAnswer).isEqualTo(answerSdp);
    }

    @Test
    public void testHandleCandidateSignal() throws Exception {
        // given
        String candidate = "candidate:123456789 1 udp 2122260223 192.168.1.1 12345 typ host";
        Map<String, Object> payload = Map.of(
            "type", "candidate",
            "targetUserId", targetUserId,
            "data", candidate
        );
        TextMessage message = new TextMessage(objectMapper.writeValueAsString(payload));

        // when
        webSocketHandler.handleTextMessage(mockSession, message);

        // then
        List<String> candidates = redisTemplate.opsForList().range("meeting:room:" + roomId + ":candidates", 0, -1);
        assertThat(candidates).contains(candidate);
    }

    @Test
    public void testClearSignalingData() throws Exception {
        // given
        String offerSdp = "v=0\r\no=- 123456789 2 IN IP4 127.0.0.1\r\ns=-\r\nt=0 0\r\n";
        String answerSdp = "v=0\r\no=- 987654321 2 IN IP4 127.0.0.1\r\ns=-\r\nt=0 0\r\n";
        String candidate = "candidate:123456789 1 udp 2122260223 192.168.1.1 12345 typ host";

        // 저장된 시그널링 데이터
        redisTemplate.opsForValue().set("meeting:room:" + roomId + ":offer", offerSdp);
        redisTemplate.opsForValue().set("meeting:room:" + roomId + ":answer", answerSdp);
        redisTemplate.opsForList().rightPush("meeting:room:" + roomId + ":candidates", candidate);

        // when
        signalingService.clearSignalingData(roomId);

        // then
        assertThat(redisTemplate.opsForValue().get("meeting:room:" + roomId + ":offer")).isNull();
        assertThat(redisTemplate.opsForValue().get("meeting:room:" + roomId + ":answer")).isNull();
        assertThat(redisTemplate.opsForList().range("meeting:room:" + roomId + ":candidates", 0, -1)).isEmpty();
    }

    @Test
    public void testGetSignalingData() throws Exception {
        // given
        String offerSdp = "v=0\r\no=- 123456789 2 IN IP4 127.0.0.1\r\ns=-\r\nt=0 0\r\n";
        String answerSdp = "v=0\r\no=- 987654321 2 IN IP4 127.0.0.1\r\ns=-\r\nt=0 0\r\n";
        String candidate = "candidate:123456789 1 udp 2122260223 192.168.1.1 12345 typ host";

        // 저장된 시그널링 데이터
        redisTemplate.opsForValue().set("meeting:room:" + roomId + ":offer", offerSdp);
        redisTemplate.opsForValue().set("meeting:room:" + roomId + ":answer", answerSdp);
        redisTemplate.opsForList().rightPush("meeting:room:" + roomId + ":candidates", candidate);

        // when
        String retrievedOffer = signalingService.getOffer(roomId);
        String retrievedAnswer = signalingService.getAnswer(roomId);
        List<String> retrievedCandidates = signalingService.getCandidates(roomId);

        // then
        assertThat(retrievedOffer).isEqualTo(offerSdp);
        assertThat(retrievedAnswer).isEqualTo(answerSdp);
        assertThat(retrievedCandidates).contains(candidate);
    }
} 