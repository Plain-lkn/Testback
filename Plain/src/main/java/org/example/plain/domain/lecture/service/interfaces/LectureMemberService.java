package org.example.plain.domain.lecture.service.interfaces;

import org.example.plain.common.ResponseField;
import org.example.plain.domain.lecture.dto.LectureResponse;
import org.example.plain.domain.user.dto.UserResponse;

import java.util.List;

public interface LectureMemberService {
    ResponseField<List<UserResponse>> getStudentList(String lectureId);
    ResponseField<List<LectureResponse>> getLectureMyList(String userId);
    ResponseField<Void> enrollLecture(String lectureId, String userId);
    ResponseField<Void> cancelEnrollment(String lectureId, String userId);
} 