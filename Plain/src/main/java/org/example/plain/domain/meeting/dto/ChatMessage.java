package org.example.plain.domain.meeting.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String type; // chat, system
    private String roomId;
    private String senderId;
    private String senderName;
    private String message;
    private LocalDateTime timestamp;
} 