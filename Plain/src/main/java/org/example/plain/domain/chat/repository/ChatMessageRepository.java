package org.example.plain.domain.chat.repository;

import org.example.plain.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    List<ChatMessage> findByChatRoomChatIdOrderByMessageStampDesc(String chatId);
    List<ChatMessage> findByChatRoomChatIdAndIsCheckedFalseOrderByMessageStampDesc(String chatId);
} 