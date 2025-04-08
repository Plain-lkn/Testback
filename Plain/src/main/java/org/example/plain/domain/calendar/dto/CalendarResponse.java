package org.example.plain.domain.calendar.dto;

import lombok.*;
import org.example.plain.common.enums.Category;
import org.example.plain.domain.calendar.entity.CalendarEntity;

import java.time.LocalDateTime;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarResponse {

    private Long calId;
    private String title;
    private String content;
    private Category category;
    private LocalDateTime dateInfo;
    private String userId;
    private String username;

    public static CalendarResponse from(CalendarEntity calendarEntity) {
        return CalendarResponse.builder()
                .calId(calendarEntity.getCalId())
                .title(calendarEntity.getTitle())
                .content(calendarEntity.getContent())
                .category(calendarEntity.getCategory())
                .dateInfo(calendarEntity.getDateInfo())
                .userId(calendarEntity.getUser().getId())
                .username(calendarEntity.getUser().getUsername())
                .build();
    }
}
