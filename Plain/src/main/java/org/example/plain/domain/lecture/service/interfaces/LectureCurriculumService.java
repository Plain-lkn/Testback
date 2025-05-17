package org.example.plain.domain.lecture.service.interfaces;

import org.example.plain.common.ResponseField;
import org.example.plain.domain.lecture.dto.lecturecurriculum.*;

import java.util.List;

public interface LectureCurriculumService {
    // Unit operations
    ResponseField<LectureUnitResponse> createUnit(String lectureId, LectureUnitRequest request);
    ResponseField<List<LectureUnitResponse>> getUnits(String lectureId);
    ResponseField<LectureUnitResponse> getUnit(String unitId);
    ResponseField<Void> deleteUnit(String unitId);

    // Chapter operations
    ResponseField<LectureChapterResponse> createChapter(String unitId, LectureChapterRequest request);
    ResponseField<List<LectureChapterResponse>> getChapters(String unitId);
    ResponseField<LectureChapterResponse> getChapter(String chapterId);
    ResponseField<Void> deleteChapter(String chapterId);

    // Curriculum operations
    ResponseField<LectureCurriculumResponse> createCurriculum(String chapterId, LectureCurriculumRequest request);
    ResponseField<List<LectureCurriculumResponse>> getCurriculums(String chapterId);
    ResponseField<LectureCurriculumResponse> getCurriculum(String curriculumId);
    ResponseField<Void> deleteCurriculum(String curriculumId);

    // Complete curriculum tree
    ResponseField<List<LectureUnitResponse>> getLectureCurriculum(String lectureId);
} 