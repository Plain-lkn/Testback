package org.example.plain.domain.meeting.integration;

import org.example.plain.domain.meeting.dto.MeetingRoomDto;
import org.example.plain.domain.meeting.dto.ParticipantState;
import org.example.plain.domain.meeting.service.MeetingService;
import org.example.plain.domain.meeting.service.ParticipantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class MeetingIntegrationTest {

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private ParticipantService participantService;

    @Autowired
    @Qualifier("meetingRoomRedisTemplate")
    private RedisTemplate<String, MeetingRoomDto> redisTemplate;

    @Test
    void testMeetingRoomLifecycle() {
        // Given
        String hostId = UUID.randomUUID().toString();
        String title = "Test Meeting";
        String participantId = UUID.randomUUID().toString();

        // When: Create meeting room
        MeetingRoomDto room = meetingService.createMeetingRoom(hostId, title);

        // Then: Verify room creation
        assertThat(room).isNotNull();
        assertThat(room.getRoomId()).isNotNull();
        assertThat(room.getTitle()).isEqualTo(title);
        assertThat(room.getHostId()).isEqualTo(hostId);


        // When: Join participant
        participantService.addParticipant(room.getRoomId(), participantId);
        participantService.updateParticipantState(room.getRoomId(), participantId, "Test User", false, false);

        // Then: Verify participant joined
        List<String> participants = participantService.getParticipants(room.getRoomId());
        assertThat(participants).contains(participantId);

        List<ParticipantState> states = participantService.getParticipantStates(room.getRoomId());
        assertThat(states).hasSize(1);
        assertThat(states.get(0).getUserId()).isEqualTo(participantId);

        // When: Leave participant
        participantService.removeParticipant(room.getRoomId(), participantId);

        // Then: Verify participant left
        participants = participantService.getParticipants(room.getRoomId());
        assertThat(participants).doesNotContain(participantId);

        // When: Close meeting room
        meetingService.closeMeetingRoom(room.getRoomId());

        // Then: Verify room is closed
        MeetingRoomDto closedRoom = redisTemplate.opsForValue().get("meeting:room:" + room.getRoomId());
        assertThat(closedRoom).isNotNull();
    }

    @Test
    void testParticipantStateChanges() {
        // Given
        String hostId = UUID.randomUUID().toString();
        String participantId = UUID.randomUUID().toString();
        MeetingRoomDto room = meetingService.createMeetingRoom(hostId, "Test Meeting");
        participantService.addParticipant(room.getRoomId(), participantId);

        // When: Update participant state
        participantService.updateParticipantState(room.getRoomId(), participantId, "Test User", true, true);

        // Then: Verify state changes
        List<ParticipantState> states = participantService.getParticipantStates(room.getRoomId());
        assertThat(states).hasSize(1);
        ParticipantState state = states.get(0);
        assertThat(state.getUserId()).isEqualTo(participantId);
        assertThat(state.isMuted()).isTrue();
        assertThat(state.isVideoOff()).isTrue();

        // When: Update state again
        participantService.updateParticipantState(room.getRoomId(), participantId, "Test User", false, false);

        // Then: Verify new state
        states = participantService.getParticipantStates(room.getRoomId());
        state = states.get(0);
        assertThat(state.isMuted()).isFalse();
        assertThat(state.isVideoOff()).isFalse();
    }
} 