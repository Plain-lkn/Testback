package org.example.plain.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {
    private final ChatMessageRequest.MessageType type;
    private final String senderId;
    private final String content;
    private final LocalDateTime timestamp;
} 