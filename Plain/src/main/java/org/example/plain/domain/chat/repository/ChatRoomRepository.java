package org.example.plain.domain.chat.repository;

import org.example.plain.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    List<ChatRoom> findByLectureId(String lectureId);
    Optional<ChatRoom> findByChatId(String chatId);
} 