package org.example.plain.domain.meeting.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.plain.domain.meeting.dto.ChatMessage;
import org.example.plain.domain.meeting.service.ChatService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final int MAX_CHAT_MESSAGES = 100;

    @Override
    public void saveMessage(ChatMessage message) {
        String key = "meeting:room:" + message.getRoomId() + ":chat";
        message.setTimestamp(LocalDateTime.now());
        redisTemplate.opsForList().rightPush(key, message.toString());
        redisTemplate.opsForList().trim(key, 0, MAX_CHAT_MESSAGES - 1);
    }

    @Override
    public List<ChatMessage> getMessages(String roomId) {
        String key = "meeting:room:" + roomId + ":chat";
        List<String> messages = redisTemplate.opsForList().range(key, 0, -1);
        return messages.stream()
                .map(msg -> ChatMessage.builder()
                        .type("chat")
                        .roomId(roomId)
                        .message(msg)
                        .build())
                .toList();
    }

    @Override
    public void clearChatHistory(String roomId) {
        String key = "meeting:room:" + roomId + ":chat";
        redisTemplate.delete(key);
    }
} 