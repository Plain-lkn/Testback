package org.example.plain.domain.lecture.dto.lecturecurriculum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.plain.domain.lecture.entity.lecture_curriculums.LectureChapterEntity;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureChapterResponse {
    private String id;
    private String title;
    private Integer sequence;
    private List<LectureCurriculumResponse> curriculums;

    public static LectureChapterResponse from(LectureChapterEntity entity) {
        return LectureChapterResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .sequence(entity.getSequence())
                .curriculums(entity.getCurriculums().stream()
                        .map(LectureCurriculumResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
} 