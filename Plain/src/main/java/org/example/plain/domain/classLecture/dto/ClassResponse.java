package org.example.plain.domain.classLecture.dto;

import lombok.Builder;
import org.example.plain.domain.classLecture.entity.ClassLecture;
import org.example.plain.common.enums.ClassType;
import org.example.plain.domain.user.entity.User;

@Builder
public record ClassResponse(
        String id,
        String nickname,
        String title,
        String classImg,
        Long maxMember,
        String code,
        String description
) {
    public static ClassResponse from(ClassLecture classLecture) {
            return new ClassResponse(
                    classLecture.getId(),
                    classLecture.getInstructor().getUsername(),
                    classLecture.getTitle(),
                    classLecture.getClassImg(),
                    classLecture.getMaxMember(),
                    classLecture.getCode(),
                    classLecture.getDescription()
            );
        }
    }
