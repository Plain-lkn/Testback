package org.example.plain.domain.meeting.service;

import org.example.plain.domain.meeting.dto.ChatMessage;
import org.example.plain.domain.meeting.dto.ParticipantState;
import org.example.plain.domain.meeting.dto.SignalMessage;
import org.example.plain.domain.meeting.service.impl.MeetingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingServiceImplTest {
    private static final String TEST_ROOM_ID = "test-room";
    private static final String TEST_USER_ID = "user1";
    private static final String TEST_USER_NAME = "Test User";
    private static final String TEST_MESSAGE = "Hello, World!";
    private static final String TEST_OFFER = "test-offer";
    private static final String TEST_ANSWER = "test-answer";
    private static final String TEST_CANDIDATE = "test-candidate";
    private static final String CHAT_TYPE = "chat";
    private static final String SYSTEM_TYPE = "system";

    @Mock
    private ChatService chatService;

    @Mock
    private ParticipantService participantService;

    @Mock
    private SignalingService signalingService;

    @InjectMocks
    private MeetingServiceImpl meetingService;

    private ChatMessage createChatMessage(String type, String message) {
        return ChatMessage.builder()
                .type(type)
                .roomId(TEST_ROOM_ID)
                .senderId(TEST_USER_ID)
                .senderName(TEST_USER_NAME)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("참가자 관리")
    class ParticipantManagement {
        @Test
        @DisplayName("참가자 입장 처리")
        void handleParticipantJoin() {
            // when
            meetingService.handleParticipantJoin(TEST_ROOM_ID, TEST_USER_ID, TEST_USER_NAME);

            // then
            verify(participantService).addParticipant(TEST_ROOM_ID, TEST_USER_ID);
            verify(participantService).updateParticipantState(TEST_ROOM_ID, TEST_USER_ID, TEST_USER_NAME, false, false);
        }

        @Test
        @DisplayName("참가자 퇴장 처리")
        void handleParticipantLeave() {
            // when
            meetingService.handleParticipantLeave(TEST_ROOM_ID, TEST_USER_ID);

            // then
            verify(participantService).removeParticipant(TEST_ROOM_ID, TEST_USER_ID);
        }

        @Test
        @DisplayName("참가자 상태 변경 처리")
        void handleStateChange() {
            // given
            boolean isMuted = true;
            boolean isVideoOff = true;

            // when
            meetingService.handleStateChange(TEST_ROOM_ID, TEST_USER_ID, TEST_USER_NAME, isMuted, isVideoOff);

            // then
            verify(participantService).updateParticipantState(TEST_ROOM_ID, TEST_USER_ID, TEST_USER_NAME, isMuted, isVideoOff);
        }

        @Test
        @DisplayName("참가자 목록 조회")
        void getParticipants() {
            // given
            List<String> expectedParticipants = Arrays.asList(TEST_USER_ID, "user2");
            when(participantService.getParticipants(TEST_ROOM_ID)).thenReturn(expectedParticipants);

            // when
            List<String> participants = meetingService.getParticipants(TEST_ROOM_ID);

            // then
            assertThat(participants).isEqualTo(expectedParticipants);
            verify(participantService).getParticipants(TEST_ROOM_ID);
        }

        @Test
        @DisplayName("참가자 상태 목록 조회")
        void getParticipantStates() {
            // given
            List<ParticipantState> expectedStates = Arrays.asList(
                    new ParticipantState(TEST_USER_ID, TEST_USER_NAME, false, false)
            );
            when(participantService.getParticipantStates(TEST_ROOM_ID)).thenReturn(expectedStates);

            // when
            List<ParticipantState> states = meetingService.getParticipantStates(TEST_ROOM_ID);

            // then
            assertThat(states).isEqualTo(expectedStates);
            verify(participantService).getParticipantStates(TEST_ROOM_ID);
        }

        @Test
        @DisplayName("방이 비어있는지 확인")
        void isRoomEmpty() {
            // given
            when(participantService.isRoomEmpty(TEST_ROOM_ID)).thenReturn(true);

            // when
            boolean isEmpty = meetingService.isRoomEmpty(TEST_ROOM_ID);

            // then
            assertThat(isEmpty).isTrue();
            verify(participantService).isRoomEmpty(TEST_ROOM_ID);
        }
    }

    @Nested
    @DisplayName("채팅")
    class Chat {
        @Test
        @DisplayName("채팅 메시지 처리")
        void handleChatMessage() {
            // given
            ChatMessage message = createChatMessage(CHAT_TYPE, TEST_MESSAGE);

            // when
            meetingService.handleChatMessage(message);

            // then
            verify(chatService).saveMessage(message);
        }

        @Test
        @DisplayName("채팅 메시지 저장")
        void saveChatMessage() {
            // given
            ChatMessage message = createChatMessage(CHAT_TYPE, TEST_MESSAGE);

            // when
            meetingService.saveChatMessage(message);

            // then
            verify(chatService).saveMessage(message);
        }

        @Test
        @DisplayName("채팅 메시지 목록 조회")
        void getChatMessages() {
            // given
            List<ChatMessage> expectedMessages = Arrays.asList(
                    createChatMessage(CHAT_TYPE, TEST_MESSAGE),
                    createChatMessage(SYSTEM_TYPE, "User joined the room")
            );
            when(chatService.getMessages(TEST_ROOM_ID)).thenReturn(expectedMessages);

            // when
            List<ChatMessage> messages = meetingService.getChatMessages(TEST_ROOM_ID);

            // then
            assertThat(messages).isEqualTo(expectedMessages);
            verify(chatService).getMessages(TEST_ROOM_ID);
        }

        @Test
        @DisplayName("채팅 기록 초기화")
        void clearRoom() {
            // when
            meetingService.clearRoom(TEST_ROOM_ID);

            // then
            verify(chatService).clearChatHistory(TEST_ROOM_ID);
        }
    }

    @Nested
    @DisplayName("시그널링")
    class Signaling {
        @Test
        @DisplayName("시그널 메시지 처리")
        void handleSignal() {
            // given
            SignalMessage message = new SignalMessage(TEST_ROOM_ID, TEST_USER_ID, "offer", TEST_OFFER);

            // when
            meetingService.handleSignal(message);

            // then
            verify(signalingService).handleSignal(message);
        }

        @Test
        @DisplayName("Offer 조회")
        void getOffer() {
            // given
            when(signalingService.getOffer(TEST_ROOM_ID)).thenReturn(TEST_OFFER);

            // when
            String offer = meetingService.getOffer(TEST_ROOM_ID);

            // then
            assertThat(offer).isEqualTo(TEST_OFFER);
            verify(signalingService).getOffer(TEST_ROOM_ID);
        }

        @Test
        @DisplayName("Answer 조회")
        void getAnswer() {
            // given
            when(signalingService.getAnswer(TEST_ROOM_ID)).thenReturn(TEST_ANSWER);

            // when
            String answer = meetingService.getAnswer(TEST_ROOM_ID);

            // then
            assertThat(answer).isEqualTo(TEST_ANSWER);
            verify(signalingService).getAnswer(TEST_ROOM_ID);
        }

        @Test
        @DisplayName("ICE Candidate 목록 조회")
        void getCandidates() {
            // given
            List<String> expectedCandidates = Arrays.asList(TEST_CANDIDATE);
            when(signalingService.getCandidates(TEST_ROOM_ID)).thenReturn(expectedCandidates);

            // when
            List<String> candidates = meetingService.getCandidates(TEST_ROOM_ID);

            // then
            assertThat(candidates).isEqualTo(expectedCandidates);
            verify(signalingService).getCandidates(TEST_ROOM_ID);
        }
    }
} 