package org.example.plain.domain.lecture.dto.lecturecurriculum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureChapterRequest {
    private String title;
    private Integer sequence;
} 