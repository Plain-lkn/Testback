package org.example.plain.domain.lecture.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.plain.domain.lecture.entity.LectureEntity;
import org.example.plain.domain.lecture.enums.LectureType;
import org.example.plain.domain.user.dto.UserResponse;

import java.time.LocalDateTime;

@Getter
@Builder
public class LectureResponse {
    private String lectureId;
    private UserResponse user;
    private LectureType lectureType;
    private String lectureName;
    private String lectureDescription;
    private Integer lecturePrice;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    public static LectureResponse from(LectureEntity lecture) {
        return LectureResponse.builder()
                .lectureId(lecture.getId())
                .user(UserResponse.chaingeUsertoUserResponse(lecture.getUser()))
                .lectureType(lecture.getLectureType())
                .lectureName(lecture.getLectureName())
                .lectureDescription(lecture.getLectureDescription())
                .lecturePrice(lecture.getLecturePrice())
                .createAt(lecture.getCreateAt())
                .updateAt(lecture.getUpdateAt())
                .build();
    }
}
