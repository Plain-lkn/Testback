package org.example.plain.domain.calendar.dto;

import lombok.*;
import org.example.plain.common.enums.Category;
import org.example.plain.domain.calendar.entity.CalendarEntity;
import org.example.plain.domain.user.entity.User;

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
    private User user;


    public static CalendarResponse from(CalendarEntity calendarEntity) {
        CalendarResponse response = new CalendarResponse();
        response.calId = calendarEntity.getCalId();
        response.title = calendarEntity.getTitle();
        response.content = calendarEntity.getContent();
        response.category = calendarEntity.getCategory();
        response.user = calendarEntity.getUser();
        response.dateInfo = calendarEntity.getDateInfo();
        return response;
    }

}
