package org.example.plain.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.plain.domain.chat.entity.ChatMessage;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private String messageId;
    private String chatId;
    private String content;
    private LocalDateTime messageStamp;
    private boolean isChecked;
    private String senderId;

    public static ChatMessageDTO fromEntity(ChatMessage message) {
        return ChatMessageDTO.builder()
                .messageId(message.getMessageId())
                .chatId(message.getChatRoom().getChatId())
                .content(message.getContent())
                .messageStamp(message.getMessageStamp())
                .isChecked(message.isChecked())
                .build();
    }
} 