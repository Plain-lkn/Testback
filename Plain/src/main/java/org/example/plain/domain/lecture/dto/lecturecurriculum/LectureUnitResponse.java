package org.example.plain.domain.lecture.dto.lecturecurriculum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.plain.domain.lecture.entity.lecture_curriculums.LectureUnitEntity;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureUnitResponse {
    private String id;
    private String title;
    private Integer sequence;
    private List<LectureChapterResponse> chapters;

    public static LectureUnitResponse from(LectureUnitEntity entity) {
        return LectureUnitResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .sequence(entity.getSequence())
                .chapters(entity.getChapters().stream()
                        .map(LectureChapterResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
} 