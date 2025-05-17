package org.example.plain.domain.lecture.dto.lecturecurriculum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureUnitRequest {
    private String title;
    private Integer sequence;
} 