package org.example.plain.domain.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.example.plain.common.enums.Category;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarRequest {
    private Long calId;
    private String title;
    private String content;
    private Category category;
    private String userId;
}
