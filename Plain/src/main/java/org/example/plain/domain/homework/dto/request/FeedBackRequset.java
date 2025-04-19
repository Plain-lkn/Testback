package org.example.plain.domain.homework.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FeedBackRequset {
    private String workId;
    private String studentId;
    private String feedback;
    private int score;
}
