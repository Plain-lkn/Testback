package org.example.plain.domain.meeting.integration;

import org.example.plain.common.enums.Role;
import org.example.plain.domain.meeting.dto.MeetingRoomDto;
import org.example.plain.domain.meeting.dto.ParticipantState;
import org.example.plain.domain.meeting.dto.SignalMessage;
import org.example.plain.domain.meeting.service.MeetingService;
import org.example.plain.domain.meeting.service.ParticipantService;
import org.example.plain.domain.meeting.service.SignalingService;
import org.example.plain.domain.user.dto.CustomUserDetails;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class MeetingRoomIntegrationTest {

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private SignalingService signalingService;

    @Autowired
    @Qualifier("meetingRoomRedisTemplate")
    private RedisTemplate<String, MeetingRoomDto> meetingRoomRedisTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    private List<String> createdRoomIds = new ArrayList<>();
    private List<User> testUsers = new ArrayList<>();

    // 테스트용 사용자 생성 메서드
    private User createTestUser(String prefix) {
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(prefix + "_" + UUID.randomUUID().toString().substring(0, 8))
                .email(prefix + "_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com")
                .password("password123")
                .role(Role.NORMAL)
                .build();
        
        User savedUser = userRepository.save(user);
        testUsers.add(savedUser);
        return savedUser;
    }

    // 테스트 인증 설정
    private void authenticateUser(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // 시그널 메시지 생성 유틸리티 메서드
    private void sendSignal(String roomId, String type, String data) {
        SignalMessage message = new SignalMessage();
        message.setRoomId(roomId);
        message.setType(type);
        message.setData(data);
        signalingService.handleSignal(message);
    }

    @BeforeEach
    void setUp() {
        // 테스트 시작 전 설정
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        // 테스트 종료 후 생성된 회의실 정리
        createdRoomIds.forEach(roomId -> {
            try {
                meetingService.closeMeetingRoom(roomId);
            } catch (Exception e) {
                // 이미 삭제된 경우 무시
            }
        });
        createdRoomIds.clear();
        
        // 생성된 사용자 제거
        testUsers.forEach(user -> {
            try {
                userRepository.delete(user);
            } catch (Exception e) {
                // 이미 삭제된 경우 무시
            }
        });
        testUsers.clear();
        
        // 보안 컨텍스트 정리
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("회의실 생성 테스트")
    class CreateMeetingRoomTests {
        
        @Test
        @DisplayName("호스트는 회의실을 성공적으로 생성할 수 있다")
        void hostCanCreateMeetingRoom() {
            // Given
            User host = createTestUser("host");
            authenticateUser(host);
            String meetingTitle = "Test Meeting " + UUID.randomUUID().toString().substring(0, 8);
            
            // When
            MeetingRoomDto room = meetingService.createMeetingRoom(host.getId(), meetingTitle);
            createdRoomIds.add(room.getRoomId());
            
            // Then
            assertThat(room).isNotNull();
            assertThat(room.getRoomId()).isNotNull();
            assertThat(room.getTitle()).isEqualTo(meetingTitle);
            assertThat(room.getHostId()).isEqualTo(host.getId());
            
            // 실제로 Redis에 저장되었는지 확인
            String roomKey = "meeting:room:" + room.getRoomId();
            assertThat(meetingRoomRedisTemplate.hasKey(roomKey)).isTrue();
        }

        @Test
        @DisplayName("회의실 생성 시 중복된 제목도 허용된다")
        void allowsDuplicateMeetingRoomTitles() {
            // Given
            User host1 = createTestUser("host1");
            User host2 = createTestUser("host2");
            authenticateUser(host1);
            String meetingTitle = "Duplicate Meeting Title";
            
            // When
            MeetingRoomDto room1 = meetingService.createMeetingRoom(host1.getId(), meetingTitle);
            createdRoomIds.add(room1.getRoomId());
            
            authenticateUser(host2);
            MeetingRoomDto room2 = meetingService.createMeetingRoom(host2.getId(), meetingTitle);
            createdRoomIds.add(room2.getRoomId());
            
            // Then
            assertThat(room1.getRoomId()).isNotEqualTo(room2.getRoomId());
            assertThat(room1.getTitle()).isEqualTo(room2.getTitle());
            assertThat(room1.getHostId()).isNotEqualTo(room2.getHostId());
        }
    }

    @Nested
    @DisplayName("참가자 관리 테스트")
    class ParticipantManagementTests {
        
        private User host;
        private User participant1;
        private User participant2;
        private MeetingRoomDto meetingRoom;
        
        @BeforeEach
        void setUpParticipantTests() {
            host = createTestUser("host");
            participant1 = createTestUser("participant1");
            participant2 = createTestUser("participant2");
            
            authenticateUser(host);
            meetingRoom = meetingService.createMeetingRoom(host.getId(), "Participant Test Room");
            createdRoomIds.add(meetingRoom.getRoomId());
        }
        
        @Test
        @DisplayName("참가자는 회의실에 성공적으로 참가할 수 있다")
        void participantsCanJoinMeetingRoom() {
            // When: 참가자 추가
            participantService.addParticipant(meetingRoom.getRoomId(), participant1.getId());
            
            // Then: 참가자 목록 확인
            List<String> participants = participantService.getParticipants(meetingRoom.getRoomId());
            assertThat(participants).contains(participant1.getId());
        }
        
        @Test
        @DisplayName("참가자의 상태는 정확하게 저장되고 업데이트된다")
        void participantStateIsCorrectlyManagedAndUpdated() {
            // Given
            participantService.addParticipant(meetingRoom.getRoomId(), participant1.getId());
            
            // When: 초기 상태 설정
            participantService.updateParticipantState(
                meetingRoom.getRoomId(),
                participant1.getId(),
                participant1.getUsername(),
                true, // 음소거 켜짐
                true  // 비디오 꺼짐
            );
            
            // Then: 초기 상태 확인
            List<ParticipantState> states = participantService.getParticipantStates(meetingRoom.getRoomId());
            assertThat(states).hasSize(1);
            ParticipantState initialState = states.get(0);
            assertThat(initialState.getUserId()).isEqualTo(participant1.getId());
            assertThat(initialState.getUserName()).isEqualTo(participant1.getUsername());
            assertThat(initialState.isMuted()).isTrue();
            assertThat(initialState.isVideoOff()).isTrue();
            
            // When: 상태 업데이트
            participantService.updateParticipantState(
                meetingRoom.getRoomId(),
                participant1.getId(),
                participant1.getUsername(),
                false,  // 음소거 꺼짐
                false   // 비디오 켜짐
            );
            
            // Then: 업데이트된 상태 확인
            states = participantService.getParticipantStates(meetingRoom.getRoomId());
            ParticipantState updatedState = states.get(0);
            assertThat(updatedState.isMuted()).isFalse();
            assertThat(updatedState.isVideoOff()).isFalse();
        }
        
        @Test
        @DisplayName("여러 참가자가 동시에 참가할 수 있다")
        void multipleParticipantsCanJoinSimultaneously() {
            // When: 여러 참가자 추가
            participantService.addParticipant(meetingRoom.getRoomId(), participant1.getId());
            participantService.addParticipant(meetingRoom.getRoomId(), participant2.getId());
            
            // 참가자 상태 설정
            participantService.updateParticipantState(
                meetingRoom.getRoomId(), 
                participant1.getId(), 
                participant1.getUsername(), 
                false, false // 음소거 꺼짐, 비디오 켜짐
            );
            participantService.updateParticipantState(
                meetingRoom.getRoomId(), 
                participant2.getId(), 
                participant2.getUsername(), 
                true, true // 음소거 켜짐, 비디오 꺼짐
            );
            
            // Then: 참가자 목록 확인
            List<String> participants = participantService.getParticipants(meetingRoom.getRoomId());
            assertThat(participants).hasSize(3);
            assertThat(participants).contains(participant1.getId(), participant2.getId());
            
            // Then: 개별 상태 확인
            List<ParticipantState> states = participantService.getParticipantStates(meetingRoom.getRoomId());
            assertThat(states).hasSize(2);
            
            ParticipantState state1 = states.stream()
                .filter(s -> s.getUserId().equals(participant1.getId()))
                .findFirst()
                .orElseThrow();
            
            ParticipantState state2 = states.stream()
                .filter(s -> s.getUserId().equals(participant2.getId()))
                .findFirst()
                .orElseThrow();
            
            assertThat(state1.isMuted()).isFalse();
            assertThat(state1.isVideoOff()).isFalse();
            
            assertThat(state2.isMuted()).isTrue();
            assertThat(state2.isVideoOff()).isTrue();
        }
        
        @Test
        @DisplayName("참가자는 회의실에서 퇴장할 수 있다")
        void participantsCanLeaveMeetingRoom() {
            // Given
            participantService.addParticipant(meetingRoom.getRoomId(), participant1.getId());
            participantService.updateParticipantState(
                meetingRoom.getRoomId(), 
                participant1.getId(), 
                participant1.getUsername(), 
                false, false
            );
            
            // 참가자가 추가되었는지 확인
            List<String> initialParticipants = participantService.getParticipants(meetingRoom.getRoomId());
            assertThat(initialParticipants).contains(participant1.getId());
            
            // When: 참가자 퇴장
            participantService.removeParticipant(meetingRoom.getRoomId(), participant1.getId());
            
            // Then: 참가자 목록에서 제거되었는지 확인
            List<String> participants = participantService.getParticipants(meetingRoom.getRoomId());
            assertThat(participants).doesNotContain(participant1.getId());
            
            // 상태 목록에서도 제거되었는지 확인
            List<ParticipantState> states = participantService.getParticipantStates(meetingRoom.getRoomId());
            assertThat(states.stream().noneMatch(s -> s.getUserId().equals(participant1.getId()))).isTrue();
        }
    }

    @Nested
    @DisplayName("WebRTC 시그널링 테스트")
    class SignalingTests {
        
        private User host;
        private User participant;
        private MeetingRoomDto meetingRoom;
        
        @BeforeEach
        void setUpSignalingTests() {
            host = createTestUser("host");
            participant = createTestUser("participant");
            
            authenticateUser(host);
            meetingRoom = meetingService.createMeetingRoom(host.getId(), "Signaling Test Room");
            createdRoomIds.add(meetingRoom.getRoomId());
            
            participantService.addParticipant(meetingRoom.getRoomId(), participant.getId());
        }
        
        @Test
        @DisplayName("SDP 오퍼와 앤서는 정확하게 저장되고 검색된다")
        void sdpOfferAndAnswerAreStoredAndRetrievedCorrectly() {
            // Given
            String offerSdp = "v=0\r\no=- 123456789 2 IN IP4 127.0.0.1\r\ns=-\r\nt=0 0\r\n";
            String answerSdp = "v=0\r\no=- 987654321 2 IN IP4 127.0.0.1\r\ns=-\r\nt=0 0\r\n";
            
            // When: SDP 오퍼 저장
            sendSignal(meetingRoom.getRoomId(), "offer", offerSdp);
            
            // Then: 오퍼 검색
            String retrievedOffer = signalingService.getOffer(meetingRoom.getRoomId());
            assertThat(retrievedOffer).isEqualTo(offerSdp);
            
            // When: SDP 앤서 저장
            sendSignal(meetingRoom.getRoomId(), "answer", answerSdp);
            
            // Then: 앤서 검색
            String retrievedAnswer = signalingService.getAnswer(meetingRoom.getRoomId());
            assertThat(retrievedAnswer).isEqualTo(answerSdp);
        }
        
        @Test
        @DisplayName("ICE 후보는 정확하게 저장되고 검색된다")
        void iceCandidatesAreStoredAndRetrievedCorrectly() {
            // Given
            String candidate1 = "candidate:123456789 1 udp 2122260223 192.168.1.1 12345 typ host";
            String candidate2 = "candidate:987654321 1 tcp 1518280447 192.168.1.1 12345 typ host tcptype active";
            
            // When: ICE 후보 추가
            sendSignal(meetingRoom.getRoomId(), "candidate", candidate1);
            sendSignal(meetingRoom.getRoomId(), "candidate", candidate2);
            
            // Then: 후보 목록 검색 및 확인
            List<String> candidates = signalingService.getCandidates(meetingRoom.getRoomId());
            assertThat(candidates).hasSize(2);
            assertThat(candidates).contains(candidate1, candidate2);
        }
        
        @Test
        @DisplayName("시그널링 데이터는 회의실 종료 시 정리된다")
        void signalingDataIsClearedWhenRoomIsClosed() {
            // Given: 시그널링 데이터 설정
            String offerSdp = "v=0\r\no=- 123456789 2 IN IP4 127.0.0.1\r\ns=-\r\nt=0 0\r\n";
            String answerSdp = "v=0\r\no=- 987654321 2 IN IP4 127.0.0.1\r\ns=-\r\nt=0 0\r\n";
            String candidate = "candidate:123456789 1 udp 2122260223 192.168.1.1 12345 typ host";
            
            sendSignal(meetingRoom.getRoomId(), "offer", offerSdp);
            sendSignal(meetingRoom.getRoomId(), "answer", answerSdp);
            sendSignal(meetingRoom.getRoomId(), "candidate", candidate);
            
            // When: 시그널링 데이터 정리
            signalingService.clearSignalingData(meetingRoom.getRoomId());
            
            // Then: 데이터가 모두 삭제되었는지 확인
            assertThat(signalingService.getOffer(meetingRoom.getRoomId())).isNull();
            assertThat(signalingService.getAnswer(meetingRoom.getRoomId())).isNull();
            assertThat(signalingService.getCandidates(meetingRoom.getRoomId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("회의실 종료 테스트")
    class MeetingClosureTests {
        
        private User host;
        private User participant;
        private MeetingRoomDto meetingRoom;
        
        @BeforeEach
        void setUpClosureTests() {
            host = createTestUser("host");
            participant = createTestUser("participant");
            
            authenticateUser(host);
            meetingRoom = meetingService.createMeetingRoom(host.getId(), "Closure Test Room");
            createdRoomIds.add(meetingRoom.getRoomId());
            
            participantService.addParticipant(meetingRoom.getRoomId(), participant.getId());
            participantService.updateParticipantState(meetingRoom.getRoomId(), participant.getId(), 
                    participant.getUsername(), false, false);
                    
            // 시그널링 데이터 추가
            sendSignal(meetingRoom.getRoomId(), "offer", "test-offer");
            sendSignal(meetingRoom.getRoomId(), "answer", "test-answer");
            sendSignal(meetingRoom.getRoomId(), "candidate", "test-candidate");
        }
        
        @Test
        @DisplayName("회의실은 성공적으로 종료되고 모든 관련 데이터가 삭제된다")
        void meetingRoomIsSuccessfullyClosedAndAllRelatedDataIsDeleted() {
            // When: 회의실 종료
            meetingService.closeMeetingRoom(meetingRoom.getRoomId());
            
            // Then: 회의실 정보가 삭제되었는지 확인
            String roomKey = "meeting:room:" + meetingRoom.getRoomId();
            assertThat(meetingRoomRedisTemplate.hasKey(roomKey)).isFalse();
            
            // 참가자 데이터가 삭제되었는지 확인
            String participantsKey = "meeting:room:" + meetingRoom.getRoomId() + ":participants";
            assertThat(redisTemplate.hasKey(participantsKey)).isFalse();
            
            // 참가자 상태 데이터가 삭제되었는지 확인
            String statesKey = "meeting:room:" + meetingRoom.getRoomId() + ":states";
            assertThat(redisTemplate.hasKey(statesKey)).isFalse();
            
            // 시그널링 데이터가 삭제되었는지 확인
            assertThat(signalingService.getOffer(meetingRoom.getRoomId())).isNull();
            assertThat(signalingService.getAnswer(meetingRoom.getRoomId())).isNull();
            assertThat(signalingService.getCandidates(meetingRoom.getRoomId())).isEmpty();
            
            // 생성된 회의실 목록에서 제거 (tearDown에서 중복 삭제 방지)
            createdRoomIds.remove(meetingRoom.getRoomId());
        }
    }
} 