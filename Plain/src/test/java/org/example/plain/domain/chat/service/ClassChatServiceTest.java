package org.example.plain.domain.chat.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassChatServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private ChatJoinRepository chatJoinRepository;
    @Mock
    private ClassLectureRepository classLectureRepository;
    @Mock
    private UserRepository userRepository;

    private ClassChatService classChatService;

    private User testUser;
    private ClassLecture testLecture;
    private ChatRoom testChatRoom;
    private ChatMessage testMessage;
    private ChatJoin testChatJoin;

    @BeforeEach
    void setUp() {
        classChatService = new ClassChatServiceImpl(
                chatRoomRepository,
                chatMessageRepository,
                chatJoinRepository,
                classLectureRepository,
                userRepository
        );

        testUser = User.builder()
                .id("testUser")
                .username("Test User")
                .email("test@example.com")
                .build();

        testLecture = ClassLecture.builder()
                .id("testLecture")
                .title("Test Lecture")
                .build();

        testChatRoom = ChatRoom.builder()
                .chatId("testChat")
                .lecture(testLecture)
                .chatName("Test Chat")
                .chatStatus(ChatStatus.ACTIVE)
                .chatStamp(LocalDateTime.now())
                .build();

        testMessage = ChatMessage.builder()
                .messageId("testMessage")
                .chatRoom(testChatRoom)
                .content("Test Message")
                .messageStamp(LocalDateTime.now())
                .isChecked(false)
                .build();

        testChatJoin = ChatJoin.builder()
                .id(new ChatJoinId("testChat", "testUser"))
                .user(testUser)
                .chatRoom(testChatRoom)
                .build();
    }

    @Test
    void createChatRoom_Success() {
        // given
        when(classLectureRepository.findById("testLecture"))
                .thenReturn(Optional.of(testLecture));
        when(chatRoomRepository.save(any(ChatRoom.class)))
                .thenReturn(testChatRoom);

        // when
        ChatRoomDTO result = classChatService.createChatRoom("testLecture", "Test Chat");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getChatName()).isEqualTo("Test Chat");
        verify(chatRoomRepository).save(any(ChatRoom.class));
    }

    @Test
    void createChatRoom_LectureNotFound() {
        // given
        when(classLectureRepository.findById("testLecture"))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> classChatService.createChatRoom("testLecture", "Test Chat"))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("강의를 찾을 수 없습니다");
    }

    @Test
    void sendMessage_Success() {
        // given
        when(chatRoomRepository.findByChatId("testChat"))
                .thenReturn(Optional.of(testChatRoom));
        when(chatJoinRepository.findByChatRoomAndUserId("testChat", "testUser"))
                .thenReturn(Optional.of(testChatJoin));
        when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenReturn(testMessage);

        // when
        ChatMessageDTO result = classChatService.sendMessage("testChat", "testUser", "Test Message");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Test Message");
        verify(chatMessageRepository).save(any(ChatMessage.class));
        verify(chatRoomRepository).save(any(ChatRoom.class));
    }

    @Test
    void sendMessage_ChatRoomNotFound() {
        // given
        when(chatRoomRepository.findByChatId("testChat"))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> classChatService.sendMessage("testChat", "testUser", "Test Message"))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("채팅방을 찾을 수 없습니다");
    }

    @Test
    void joinChatRoom_Success() {
        // given
        when(chatRoomRepository.findByChatId("testChat"))
                .thenReturn(Optional.of(testChatRoom));
        when(userRepository.findById("testUser"))
                .thenReturn(Optional.of(testUser));
        when(chatJoinRepository.findById(any(ChatJoinId.class)))
                .thenReturn(Optional.empty());

        // when
        classChatService.joinChatRoom("testChat", "testUser");

        // then
        verify(chatJoinRepository).save(any(ChatJoin.class));
    }

    @Test
    void joinChatRoom_AlreadyJoined() {
        // given
        when(chatRoomRepository.findByChatId("testChat"))
                .thenReturn(Optional.of(testChatRoom));
        when(userRepository.findById("testUser"))
                .thenReturn(Optional.of(testUser));
        when(chatJoinRepository.findById(any(ChatJoinId.class)))
                .thenReturn(Optional.of(testChatJoin));

        // when & then
        assertThatThrownBy(() -> classChatService.joinChatRoom("testChat", "testUser"))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("이미 참여중인 채팅방입니다");
    }

    @Test
    void getMessages_Success() {
        // given
        List<ChatMessage> messages = Arrays.asList(testMessage);
        when(chatMessageRepository.findByChatRoomChatIdOrderByMessageStampDesc("testChat"))
                .thenReturn(messages);

        // when
        List<ChatMessageDTO> result = classChatService.getMessages("testChat");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("Test Message");
    }

    @Test
    void markMessagesAsRead_Success() {
        // given
        List<ChatMessage> unreadMessages = Arrays.asList(testMessage);
        when(chatJoinRepository.findByChatRoomAndUserId("testChat", "testUser"))
                .thenReturn(Optional.of(testChatJoin));
        when(chatMessageRepository.findByChatRoomChatIdAndIsCheckedFalseOrderByMessageStampDesc("testChat"))
                .thenReturn(unreadMessages);

        // when
        classChatService.markMessagesAsRead("testChat", "testUser");

        // then
        verify(chatMessageRepository).saveAll(anyList());
        assertThat(unreadMessages.get(0).isChecked()).isTrue();
    }
} 