package org.example.plain.domain.meeting.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.plain.domain.meeting.dto.*;

import org.example.plain.domain.meeting.service.ChatService;
import org.example.plain.domain.meeting.service.MeetingService;
import org.example.plain.domain.meeting.service.ParticipantService;
import org.example.plain.domain.meeting.service.SignalingService;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {
    private final ChatService chatService;
    private final ParticipantService participantService;
    private final SignalingService signalingService;
    private final RedisTemplate<String, MeetingRoomDto> redisTemplate;
   
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MeetingRoomDto createMeetingRoom(String hostId, String title) {
        log.info("Creating new meeting room for host: {}, title: {}", hostId, title);
        
        String roomId = UUID.randomUUID().toString();
        MeetingRoomDto meetingRoom = MeetingRoomDto.create(roomId, hostId, title);
        
        // Redis에 회의실 저장
        redisTemplate.opsForValue().set("meeting:room:" + roomId, meetingRoom);
        
        // 호스트를 참여자로 추가
        participantService.addParticipant(roomId, hostId);
        log.info("Meeting room created successfully: {}", roomId);
        
        return meetingRoom;
    }

    @Override
    @Transactional
    public void closeMeetingRoom(String roomId) {
        log.info("Closing meeting room: {}", roomId);
        
        // 회의실 상태 업데이트
        MeetingRoomDto meetingRoom = redisTemplate.opsForValue().get("meeting:room:" + roomId);
        if (meetingRoom != null) {
            meetingRoom.close();
            redisTemplate.opsForValue().set("meeting:room:" + roomId, meetingRoom);
        }
        
        // 채팅 기록 삭제
        chatService.clearChatHistory(roomId);
        
        // 시그널링 데이터 삭제
        signalingService.clearSignalingData(roomId);
        
        // 참가자 데이터 삭제
        participantService.removeAllParticipants(roomId);
        
        log.info("Meeting room closed successfully: {}", roomId);
    }

    @Override
    public void handleParticipantJoin(String roomId, String userId, String userName) {
        participantService.addParticipant(roomId, userId);
        participantService.updateParticipantState(roomId, userId, userName, false, false);
    }

    @Override
    public void handleParticipantLeave(String roomId, String userId) {
        participantService.removeParticipant(roomId, userId);
    }

    @Override
    public void handleStateChange(String roomId, String userId, String userName, boolean isMuted, boolean isVideoOff) {
        participantService.updateParticipantState(roomId, userId, userName, isMuted, isVideoOff);
    }

    @Override
    public void handleChatMessage(ChatMessage message) {
        chatService.saveMessage(message);
    }

    @Override
    public List<ParticipantState> getParticipantStates(String roomId) {
        return participantService.getParticipantStates(roomId);
    }

    @Override
    public List<ChatMessage> getChatMessages(String roomId) {
        return chatService.getMessages(roomId);
    }

    @Override
    public boolean isRoomEmpty(String roomId) {
        return participantService.isRoomEmpty(roomId);
    }

    @Override
    public void clearRoom(String roomId) {
        chatService.clearChatHistory(roomId);
    }

    @Override
    public void addParticipant(String roomId, String userId) {
        participantService.addParticipant(roomId, userId);
    }

    @Override
    public void updateParticipantState(String roomId, String userId, String userName, boolean isMuted, boolean isVideoOff) {
        participantService.updateParticipantState(roomId, userId, userName, isMuted, isVideoOff);
    }

    @Override
    public void handleSignal(SignalMessage message) {
        signalingService.handleSignal(message);
    }

    @Override
    public void saveChatMessage(ChatMessage message) {
        chatService.saveMessage(message);
    }

    @Override
    public List<String> getParticipants(String roomId) {
        return participantService.getParticipants(roomId);
    }

    @Override
    public String getOffer(String roomId) {
        return signalingService.getOffer(roomId);
    }

    @Override
    public String getAnswer(String roomId) {
        return signalingService.getAnswer(roomId);
    }

    @Override
    public List<String> getCandidates(String roomId) {
        return signalingService.getCandidates(roomId);
    }
} 