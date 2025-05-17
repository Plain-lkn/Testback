package org.example.plain.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.plain.domain.chat.entity.ChatRoom;
import org.example.plain.domain.chat.entity.ChatStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDTO {
    private String chatId;
    private String lectureId;
    private String chatName;
    private ChatStatus chatStatus;
    private LocalDateTime chatStamp;
    private int unreadCount;

    public static ChatRoomDTO fromEntity(ChatRoom chatRoom) {
        return ChatRoomDTO.builder()
                .chatId(chatRoom.getChatId())
                .lectureId(chatRoom.getLecture().getId())
                .chatName(chatRoom.getChatName())
                .chatStatus(chatRoom.getChatStatus())
                .chatStamp(chatRoom.getChatStamp())
                .build();
    }
} 