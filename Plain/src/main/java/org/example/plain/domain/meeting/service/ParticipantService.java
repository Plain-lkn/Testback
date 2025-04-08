package org.example.plain.domain.meeting.service;

import org.example.plain.domain.meeting.dto.ParticipantState;

import java.util.List;

public interface ParticipantService {
    /**
     * 참가자를 회의실에 추가합니다.
     * @param roomId 회의실 ID
     * @param userId 참가자 ID
     */
    void addParticipant(String roomId, String userId);

    /**
     * 참가자를 회의실에서 제거합니다.
     * @param roomId 회의실 ID
     * @param userId 참가자 ID
     */
    void removeParticipant(String roomId, String userId);

    /**
     * 회의실의 모든 참가자를 제거합니다.
     * @param roomId 회의실 ID
     */
    void removeAllParticipants(String roomId);

    /**
     * 참가자의 상태를 업데이트합니다.
     * @param roomId 회의실 ID
     * @param userId 참가자 ID
     * @param userName 참가자 이름
     * @param isMuted 음소거 여부
     * @param isVideoOff 비디오 꺼짐 여부
     */
    void updateParticipantState(String roomId, String userId, String userName, boolean isMuted, boolean isVideoOff);

    /**
     * 회의실의 모든 참가자 상태를 반환합니다.
     * @param roomId 회의실 ID
     * @return 참가자 상태 목록
     */
    List<ParticipantState> getParticipantStates(String roomId);

    /**
     * 회의실의 모든 참가자 ID를 반환합니다.
     * @param roomId 회의실 ID
     * @return 참가자 ID 목록
     */
    List<String> getParticipants(String roomId);

    /**
     * 회의실이 비어있는지 확인합니다.
     * @param roomId 회의실 ID
     * @return 비어있으면 true
     */
    boolean isRoomEmpty(String roomId);
} 