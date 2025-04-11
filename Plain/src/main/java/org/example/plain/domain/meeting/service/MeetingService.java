package org.example.plain.domain.meeting.service;

import org.example.plain.domain.meeting.dto.ChatMessage;
import org.example.plain.domain.meeting.dto.MeetingRoomDto;
import org.example.plain.domain.meeting.dto.ParticipantState;
import org.example.plain.domain.meeting.dto.SignalMessage;

import java.util.List;

public interface MeetingService {
    /**
     * 새로운 회의실을 생성합니다.
     * @param hostId 호스트 ID
     * @param title 회의실 제목
     * @return 생성된 회의실 정보
     */
    MeetingRoomDto createMeetingRoom(String hostId, String title);

    /**
     * 회의실을 종료하고 모든 관련 데이터를 삭제합니다.
     * @param roomId 회의실 ID
     */
    void closeMeetingRoom(String roomId);

    void handleParticipantJoin(String roomId, String userId, String userName);
    void handleParticipantLeave(String roomId, String userId);
    void handleStateChange(String roomId, String userId, String userName, boolean isMuted, boolean isVideoOff);
    void handleChatMessage(ChatMessage message);
    List<ParticipantState> getParticipantStates(String roomId);
    List<ChatMessage> getChatMessages(String roomId);
    boolean isRoomEmpty(String roomId);
    void clearRoom(String roomId);
    
    // 추가된 메서드들
    void addParticipant(String roomId, String userId);
    void updateParticipantState(String roomId, String userId, String userName, boolean isMuted, boolean isVideoOff);
    void handleSignal(SignalMessage message);
    void saveChatMessage(ChatMessage message);
    List<String> getParticipants(String roomId);
    
    // 컨트롤러에서 사용하는 메서드들
    String getOffer(String roomId);
    String getAnswer(String roomId);
    List<String> getCandidates(String roomId);

    /**
     * 회의실 정보를 조회합니다.
     * @param roomId 회의실 ID
     * @return 회의실 정보
     */
    MeetingRoomDto getMeetingRoom(String roomId);
} 