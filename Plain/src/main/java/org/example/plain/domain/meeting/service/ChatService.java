package org.example.plain.domain.meeting.service;

import org.example.plain.domain.meeting.dto.ChatMessage;

import java.util.List;

public interface ChatService {
    void saveMessage(ChatMessage message);
    List<ChatMessage> getMessages(String roomId);
    void clearChatHistory(String roomId);
} 