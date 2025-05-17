package org.example.plain.domain.lecture.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.plain.domain.lecture.enums.LectureType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LectureRequest {

    @NotNull(message = "강의 타입은 필수입니다.")
    private LectureType lectureType;

    @NotBlank(message = "강의명은 필수입니다.")
    private String lectureName;

    private String lectureDescription;

    @NotNull(message = "강의 가격은 필수입니다.")
    private Integer lecturePrice;
}
