package org.example.plain.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatMessageRequest {
    private MessageType type;
    private String chatId;
    private String senderId;
    private String content;
    private String senderName;
    private LocalDateTime timestamp;

    public enum MessageType {
        ENTER,      // 채팅방 입장
        TALK,       // 대화
        LEAVE,      // 채팅방 퇴장
        READ        // 메시지 읽음
    }
} 