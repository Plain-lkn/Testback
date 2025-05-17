package org.example.plain.domain.lecture.entity.member;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class LectureMemberId implements Serializable {
    private String user;    // User의 ID
    private String lecture; // LectureEntity의 ID
} 