package org.example.plain.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.plain.domain.chat.dto.ChatMessageDTO;
import org.example.plain.domain.chat.dto.ChatRoomDTO;
import org.example.plain.domain.chat.entity.*;
import org.example.plain.domain.chat.entity.id.ChatJoinId;
import org.example.plain.domain.chat.repository.ChatJoinRepository;
import org.example.plain.domain.chat.repository.ChatMessageRepository;
import org.example.plain.domain.chat.repository.ChatRoomRepository;
import org.example.plain.domain.classLecture.entity.ClassLecture;
import org.example.plain.domain.classLecture.repository.ClassLectureRepository;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClassChatServiceImpl implements ClassChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatJoinRepository chatJoinRepository;
    private final ClassLectureRepository classLectureRepository;
    private final UserRepository userRepository;

    @Override
    public ChatRoomDTO createChatRoom(String classId, String chatName) {
        ClassLecture lecture = classLectureRepository.findById(classId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "클래스를 찾을 수 없습니다."));

        ChatRoom chatRoom = ChatRoom.builder()
                .chatId(UUID.randomUUID().toString())
                .lecture(lecture)
                .chatName(chatName)
                .chatStatus(ChatStatus.ACTIVE)
                .chatStamp(LocalDateTime.now())
                .build();

        chatRoomRepository.save(chatRoom);
        return ChatRoomDTO.fromEntity(chatRoom);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomDTO> getChatRoomsByLecture(String lectureId) {
        return chatRoomRepository.findByLectureId(lectureId).stream()
                .map(ChatRoomDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ChatRoomDTO getChatRoom(String chatId) {
        ChatRoom chatRoom = chatRoomRepository.findByChatId(chatId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));
        return ChatRoomDTO.fromEntity(chatRoom);
    }

    @Override
    public void deleteChatRoom(String chatId) {
        ChatRoom chatRoom = chatRoomRepository.findByChatId(chatId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));
        chatRoom.setChatStatus(ChatStatus.DELETED);
        chatRoomRepository.save(chatRoom);
    }

    @Override
    public ChatMessageDTO sendMessage(String chatId, String userId, String content) {
        ChatRoom chatRoom = chatRoomRepository.findByChatId(chatId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        // 채팅방 참여 여부 확인
        chatJoinRepository.findByChatRoomAndUserId(chatId, userId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.FORBIDDEN, "채팅방에 참여하지 않은 사용자입니다."));

        ChatMessage message = ChatMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .chatRoom(chatRoom)
                .content(content)
                .messageStamp(LocalDateTime.now())
                .isChecked(false)
                .build();

        chatMessageRepository.save(message);
        
        // 채팅방 최종 업데이트 시간 갱신
        chatRoom.setChatStamp(LocalDateTime.now());
        chatRoomRepository.save(chatRoom);

        return ChatMessageDTO.fromEntity(message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getMessages(String chatId) {
        return chatMessageRepository.findByChatRoomChatIdOrderByMessageStampDesc(chatId).stream()
                .map(ChatMessageDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getUnreadMessages(String chatId) {
        return chatMessageRepository.findByChatRoomChatIdAndIsCheckedFalseOrderByMessageStampDesc(chatId).stream()
                .map(ChatMessageDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void markMessagesAsRead(String chatId, String userId) {
        // 채팅방 참여 여부 확인
        chatJoinRepository.findByChatRoomAndUserId(chatId, userId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.FORBIDDEN, "채팅방에 참여하지 않은 사용자입니다."));

        List<ChatMessage> unreadMessages = chatMessageRepository.findByChatRoomChatIdAndIsCheckedFalseOrderByMessageStampDesc(chatId);
        unreadMessages.forEach(message -> message.setChecked(true));
        chatMessageRepository.saveAll(unreadMessages);
    }

    @Override
    public void joinChatRoom(String chatId, String userId) {
        ChatRoom chatRoom = chatRoomRepository.findByChatId(chatId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 이미 참여중인지 확인
        ChatJoinId chatJoinId = new ChatJoinId(chatId, userId);
        if (chatJoinRepository.findById(chatJoinId).isPresent()) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "이미 참여중인 채팅방입니다.");
        }

        ChatJoin chatJoin = ChatJoin.builder()
                .id(chatJoinId)
                .user(user)
                .chatRoom(chatRoom)
                .build();

        chatJoinRepository.save(chatJoin);
    }

    @Override
    public void leaveChatRoom(String chatId, String userId) {
        ChatJoinId chatJoinId = new ChatJoinId(chatId, userId);
        ChatJoin chatJoin = chatJoinRepository.findById(chatJoinId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "채팅방 참여 정보를 찾을 수 없습니다."));

        chatJoinRepository.delete(chatJoin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomDTO> getMyChatRooms(String userId) {
        List<ChatJoin> chatJoins = chatJoinRepository.findByIdUserId(userId);
        return chatJoins.stream()
                .map(join -> ChatRoomDTO.fromEntity(join.getChatRoom()))
                .collect(Collectors.toList());
    }
} 