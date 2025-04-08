package org.example.plain.domain.meeting.service;

import org.example.plain.domain.meeting.dto.SignalMessage;

import java.util.List;

public interface SignalingService {
    void handleSignal(SignalMessage message);
    String getOffer(String roomId);
    String getAnswer(String roomId);
    List<String> getCandidates(String roomId);
    void clearSignalingData(String roomId);
} 