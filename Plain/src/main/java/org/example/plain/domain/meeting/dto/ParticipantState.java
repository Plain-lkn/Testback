package org.example.plain.domain.meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantState {
    private String userId;
    private String userName;
    private boolean isMuted;
    private boolean isVideoOff;
} 