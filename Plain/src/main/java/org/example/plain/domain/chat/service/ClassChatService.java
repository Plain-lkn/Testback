package org.example.plain.domain.chat.service;

import org.example.plain.domain.chat.dto.ChatMessageDTO;
import org.example.plain.domain.chat.dto.ChatRoomDTO;

import java.util.List;

public interface ClassChatService {
    // 채팅방 관련
    ChatRoomDTO createChatRoom(String lectureId, String chatName);
    List<ChatRoomDTO> getChatRoomsByLecture(String lectureId);
    ChatRoomDTO getChatRoom(String chatId);
    void deleteChatRoom(String chatId);

    // 채팅 메시지 관련
    ChatMessageDTO sendMessage(String chatId, String userId, String content);
    List<ChatMessageDTO> getMessages(String chatId);
    List<ChatMessageDTO> getUnreadMessages(String chatId);
    void markMessagesAsRead(String chatId, String userId);

    // 채팅방 참여 관련
    void joinChatRoom(String chatId, String userId);
    void leaveChatRoom(String chatId, String userId);
    List<ChatRoomDTO> getMyChatRooms(String userId);
} 