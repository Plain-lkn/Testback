package org.example.plain.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.plain.domain.classLecture.entity.ClassLecture;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chat_room")
public class ChatRoom {
    @Id
    @Column(name = "chat_id")
    private String chatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private ClassLecture lecture;

    @Column(name = "chat_name")
    private String chatName;

    @Column(name = "chat_status")
    @Enumerated(EnumType.STRING)
    private ChatStatus chatStatus;

    @Column(name = "chat_stamp")
    private LocalDateTime chatStamp;
} 