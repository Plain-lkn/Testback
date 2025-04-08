package org.example.plain.domain.meeting.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.plain.domain.meeting.dto.SignalMessage;
import org.example.plain.domain.meeting.service.SignalingService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SignalingServiceImpl implements SignalingService {
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void handleSignal(SignalMessage message) {
        String roomKey = "meeting:room:" + message.getRoomId();
        
        switch (message.getType()) {
            case "offer":
                redisTemplate.opsForValue().set(roomKey + ":offer", message.getData());
                break;
            case "answer":
                redisTemplate.opsForValue().set(roomKey + ":answer", message.getData());
                break;
            case "candidate":
                redisTemplate.opsForList().rightPush(roomKey + ":candidates", message.getData());
                break;
        }
    }

    @Override
    public String getOffer(String roomId) {
        String key = "meeting:room:" + roomId + ":offer";
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public String getAnswer(String roomId) {
        String key = "meeting:room:" + roomId + ":answer";
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public List<String> getCandidates(String roomId) {
        String key = "meeting:room:" + roomId + ":candidates";
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    @Override
    public void clearSignalingData(String roomId) {
        String roomKey = "meeting:room:" + roomId;
        redisTemplate.delete(roomKey + ":offer");
        redisTemplate.delete(roomKey + ":answer");
        redisTemplate.delete(roomKey + ":candidates");
    }
} 