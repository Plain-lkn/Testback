package org.example.plain.domain.lecture.dto.lecturecurriculum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.plain.domain.lecture.entity.lecture_curriculums.LectureVideoCurriculumEntity;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureVideoResponse {
    private String id;
    private String parent;
    private String filename;
    private String filepath;
    private Integer playTime;
    private String videoType;

    public static LectureVideoResponse from(LectureVideoCurriculumEntity entity) {
        return LectureVideoResponse.builder()
                .id(entity.getVideoCurriculumId())
                .parent(entity.getCurriculum().getId())
                .filename(entity.getFilename())
                .filepath(entity.getFilePath())
                .playTime(entity.getPlayTime())
                .videoType(entity.getVideoType())
                .build();
    }
} 