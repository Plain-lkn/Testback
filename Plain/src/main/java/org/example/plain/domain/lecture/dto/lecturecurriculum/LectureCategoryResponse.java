package org.example.plain.domain.lecture.dto.lecturecurriculum;

import lombok.Builder;
import lombok.Getter;
import org.example.plain.domain.lecture.entity.LectureCategoryEntity;

@Getter
@Builder
public class LectureCategoryResponse {
    private String id;
    private String name;

    public static LectureCategoryResponse from(LectureCategoryEntity entity) {
        return LectureCategoryResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
} 