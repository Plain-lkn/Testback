package org.example.plain.domain.lecture.service.interfaces;

import org.example.plain.common.ResponseField;
import org.example.plain.domain.lecture.dto.lecturecurriculum.LectureCategoryResponse;
import org.example.plain.domain.lecture.dto.LectureResponse;

import java.util.List;

public interface LectureCategoryService {
    ResponseField<List<LectureCategoryResponse>> getCategoryList();
    ResponseField<List<LectureResponse>> getLecturesByCategory(String categoryId);
    ResponseField<Void> setLectureCategory(String lectureId, String categoryId);
    ResponseField<LectureCategoryResponse> createCategory(String categoryName);
    ResponseField<Void> deleteCategory(String categoryId);
} 