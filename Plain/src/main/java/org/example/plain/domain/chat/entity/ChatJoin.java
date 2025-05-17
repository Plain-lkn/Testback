package org.example.plain.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.plain.domain.chat.entity.id.ChatJoinId;
import org.example.plain.domain.user.entity.User;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chat_member")
public class ChatJoin {
    @EmbeddedId
    private ChatJoinId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("chatId")
    @JoinColumn(name = "chat_id")
    private ChatRoom chatRoom;
} 