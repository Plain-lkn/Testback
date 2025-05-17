package org.example.plain.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.plain.domain.chat.dto.ChatMessageDTO;
import org.example.plain.domain.chat.dto.ChatRoomDTO;
import org.example.plain.domain.chat.service.ClassChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ClassChatService classChatService;

    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomDTO> createChatRoom(
            @RequestParam String lectureId,
            @RequestParam String chatName) {
        return ResponseEntity.ok(classChatService.createChatRoom(lectureId, chatName));
    }

    @GetMapping("/rooms/lecture/{lectureId}")
    public ResponseEntity<List<ChatRoomDTO>> getChatRoomsByLecture(@PathVariable String lectureId) {
        return ResponseEntity.ok(classChatService.getChatRoomsByLecture(lectureId));
    }

    @GetMapping("/rooms/{chatId}")
    public ResponseEntity<ChatRoomDTO> getChatRoom(@PathVariable String chatId) {
        return ResponseEntity.ok(classChatService.getChatRoom(chatId));
    }

    @DeleteMapping("/rooms/{chatId}")
    public ResponseEntity<Void> deleteChatRoom(@PathVariable String chatId) {
        classChatService.deleteChatRoom(chatId);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/rooms/{chatId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getMessages(@PathVariable String chatId) {
        return ResponseEntity.ok(classChatService.getMessages(chatId));
    }

    @GetMapping("/rooms/{chatId}/messages/unread")
    public ResponseEntity<List<ChatMessageDTO>> getUnreadMessages(@PathVariable String chatId) {
        return ResponseEntity.ok(classChatService.getUnreadMessages(chatId));
    }

    @PostMapping("/rooms/{chatId}/messages/read")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable String chatId,
            Authentication authentication) {
        classChatService.markMessagesAsRead(chatId, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rooms/{chatId}/join")
    public ResponseEntity<Void> joinChatRoom(
            @PathVariable String chatId,
            Authentication authentication) {
        classChatService.joinChatRoom(chatId, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rooms/{chatId}/leave")
    public ResponseEntity<Void> leaveChatRoom(
            @PathVariable String chatId,
            Authentication authentication) {
        classChatService.leaveChatRoom(chatId, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rooms/my")
    public ResponseEntity<List<ChatRoomDTO>> getMyChatRooms(Authentication authentication) {
        return ResponseEntity.ok(classChatService.getMyChatRooms(authentication.getName()));
    }
} 