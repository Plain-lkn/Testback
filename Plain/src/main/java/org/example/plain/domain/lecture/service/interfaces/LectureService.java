package org.example.plain.domain.lecture.service.interfaces;

import org.example.plain.common.ResponseField;
import org.example.plain.domain.lecture.dto.LectureRequest;
import org.example.plain.domain.lecture.dto.LectureResponse;

import java.util.List;

public interface LectureService {
    ResponseField<List<LectureResponse>> getLectureList();
    ResponseField<LectureResponse> getLectureDetailShop(String lectureId);
    ResponseField<LectureResponse> getLectureDetail(String lectureId);
    ResponseField<LectureResponse> createLecture(LectureRequest request, String userId);
    ResponseField<LectureResponse> updateLecture(String lectureId, LectureRequest request, String userId);
    void deleteLecture(String lectureId, String userId);
} 