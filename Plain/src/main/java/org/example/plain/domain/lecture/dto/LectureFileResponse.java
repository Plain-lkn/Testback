package org.example.plain.domain.lecture.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.plain.domain.lecture.entity.LectureFileEntity;

@Getter
@Builder
public class LectureFileResponse {
    private String id;
    private String filename;
    private String filePath;

    public static LectureFileResponse from(LectureFileEntity entity) {
        return LectureFileResponse.builder()
                .id(entity.getId())
                .filename(entity.getFilename())
                .filePath(entity.getFilePath())
                .build();
    }
} 