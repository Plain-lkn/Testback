package org.example.plain.domain.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.plain.common.enums.Category;
import org.example.plain.domain.user.entity.User;

import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CalendarRequest {

    private Long calId;
    private String title;
    private String content;
    private Category category;
    private User user;
    private LocalDateTime date;

}
