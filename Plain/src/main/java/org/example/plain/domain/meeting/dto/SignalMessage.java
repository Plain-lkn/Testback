package org.example.plain.domain.meeting.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalMessage {
    private String type; // offer, answer, candidate
    private String roomId;
    private String senderId;
    private String data; // SDP 또는 ICE candidate 데이터
} 