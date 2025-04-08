package org.example.plain.domain.meeting.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.plain.domain.meeting.dto.ParticipantState;
import org.example.plain.domain.meeting.service.ParticipantService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {
    private final Map<String, List<String>> roomParticipants = new ConcurrentHashMap<>();
    private final Map<String, ParticipantState> participantStates = new ConcurrentHashMap<>();

    @Override
    public void addParticipant(String roomId, String participantId) {
        roomParticipants.computeIfAbsent(roomId, k -> new ArrayList<>()).add(participantId);
    }

    @Override
    public void removeParticipant(String roomId, String participantId) {
        List<String> participants = roomParticipants.get(roomId);
        if (participants != null) {
            participants.remove(participantId);
            if (participants.isEmpty()) {
                roomParticipants.remove(roomId);
            }
        }
        participantStates.remove(roomId + ":" + participantId);
    }

    @Override
    public List<String> getParticipants(String roomId) {
        return roomParticipants.getOrDefault(roomId, new ArrayList<>());
    }

    @Override
    public void updateParticipantState(String roomId, String userId, String userName, boolean isMuted, boolean isVideoOff) {
        ParticipantState state = ParticipantState.builder()
                .userId(userId)
                .userName(userName)
                .isMuted(isMuted)
                .isVideoOff(isVideoOff)
                .build();
        participantStates.put(roomId + ":" + userId, state);
    }

    @Override
    public List<ParticipantState> getParticipantStates(String roomId) {
        return roomParticipants.getOrDefault(roomId, new ArrayList<>()).stream()
                .map(userId -> participantStates.get(roomId + ":" + userId))
                .filter(state -> state != null)
                .toList();
    }

    @Override
    public boolean isRoomEmpty(String roomId) {
        List<String> participants = roomParticipants.get(roomId);
        return participants == null || participants.isEmpty();
    }

    @Override
    public void removeAllParticipants(String roomId) {
        roomParticipants.remove(roomId);
        participantStates.keySet().removeIf(key -> key.startsWith(roomId + ":"));
    }
} 