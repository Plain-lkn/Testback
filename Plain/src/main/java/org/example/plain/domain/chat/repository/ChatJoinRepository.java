package org.example.plain.domain.chat.repository;

import org.example.plain.domain.chat.entity.ChatJoin;
import org.example.plain.domain.chat.entity.id.ChatJoinId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatJoinRepository extends JpaRepository<ChatJoin, ChatJoinId> {

    @Query("SELECT c from ChatJoin c WHERE c.user.id = :userId")
    List<ChatJoin> findByIdUserId(String userId);

    @Query("SELECT c from ChatJoin c WHERE c.user.id = :userId AND c.chatRoom.chatId = :chatId")
    Optional<ChatJoin> findByChatRoomAndUserId(String chatId, String userId);

    @Query("SELECT c from ChatJoin c WHERE c.chatRoom.chatId = :chatId")
    List<ChatJoin> findByIdChatId(String chatId);
} 