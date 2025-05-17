package org.example.plain.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chat_message")
public class ChatMessage {
    @Id
    @Column(name = "mes_id")
    private String messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private ChatRoom chatRoom;

    @Column(name = "mes_content")
    private String content;

    @Column(name = "mes_stamp")
    private LocalDateTime messageStamp;

    @Column(name = "mes_check")
    private boolean isChecked;
} 