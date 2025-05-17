package org.example.plain.domain.lecture.dto.lecturecurriculum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.plain.domain.lecture.entity.lecture_curriculums.LectureCurriculumEntity;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureCurriculumResponse {
    private String id;
    private String title;
    private Integer sequence;
    private String description;
    private LectureVideoResponse video;

    public static LectureCurriculumResponse from(LectureCurriculumEntity entity) {
        return LectureCurriculumResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .sequence(entity.getSequence())
                .description(entity.getDescription())
                .video(entity.getVideo() != null ? LectureVideoResponse.from(entity.getVideo()) : null)
                .build();
    }
} 