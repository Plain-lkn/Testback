package org.example.plain.domain.meeting.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeetingRoomDto implements Serializable {
    private String roomId;
    private String hostId;
    private String title;
    private LocalDateTime createdAt;
    private List<String> participants;
    
    @JsonProperty("active")
    private boolean isActive;

    private String status;

    public static MeetingRoomDto create(String roomId, String hostId, String title) {
        return MeetingRoomDto.builder()
                .roomId(roomId)
                .hostId(hostId)
                .title(title)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .status("OPEN")
                .build();
    }

    public void close() {
        this.isActive = false;
        this.status = "CLOSED";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 