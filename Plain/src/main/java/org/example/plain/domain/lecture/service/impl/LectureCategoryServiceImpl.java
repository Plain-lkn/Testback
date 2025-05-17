//package org.example.plain.domain.lecture.service.impl;
//
//import lombok.RequiredArgsConstructor;
//import org.example.plain.common.ResponseField;
//import org.example.plain.common.enums.Message;
//import org.example.plain.domain.lecture.dto.lecturecurriculum.LectureCategoryResponse;
//import org.example.plain.domain.lecture.dto.LectureResponse;
//import org.example.plain.domain.lecture.entity.LectureCategoryEntity;
//import org.example.plain.domain.lecture.entity.LectureEntity;
//import org.example.plain.domain.lecture.repository.LectureCategoryRepository;
//import org.example.plain.domain.lecture.repository.LectureRepository;
//import org.example.plain.domain.lecture.service.interfaces.LectureCategoryService;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class LectureCategoryServiceImpl implements LectureCategoryService {
//
//    private final LectureRepository lectureRepository;
//    private final LectureCategoryRepository lectureCategoryRepository;
//
//    @Override
//    public ResponseField<List<LectureCategoryResponse>> getCategoryList() {
//        List<LectureCategoryResponse> categories = lectureCategoryRepository.findAll()
//                .stream()
//                .map(LectureCategoryResponse::from)
//                .collect(Collectors.toList());
//
//        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, categories);
//    }
//
//    @Override
//    public ResponseField<List<LectureResponse>> getLecturesByCategory(String categoryId) {
//        List<LectureResponse> lectures = lectureCategoryRepository.findById(categoryId)
//                .stream()
//                .map(LectureCategoryEntity::getLecture)
//                .map(LectureResponse::from)
//                .collect(Collectors.toList());
//
//        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, lectures);
//    }
//
//    @Override
//    @Transactional
//    public ResponseField<Void> setLectureCategory(String lectureId, String categoryId) {
//        LectureEntity lecture = lectureRepository.findById(lectureId)
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));
//
//        LectureCategoryEntity category = lectureCategoryRepository.findById(categoryId)
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
//
//
//        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, null);
//    }
//
//    @Override
//    @Transactional
//    public ResponseField<LectureCategoryResponse> createCategory(String categoryName) {
//        if (lectureCategoryRepository.existsByName(categoryName)) {
//            throw new IllegalArgumentException("이미 존재하는 카테고리입니다.");
//        }
//
//        LectureCategoryEntity category = LectureCategoryEntity.create(categoryName);
//        LectureCategoryEntity savedCategory = lectureCategoryRepository.save(category);
//
//        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, LectureCategoryResponse.from(savedCategory));
//    }
//
//    @Override
//    @Transactional
//    public ResponseField<Void> deleteCategory(String categoryId) {
//        LectureCategoryEntity category = lectureCategoryRepository.findById(categoryId)
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
//
//        lectureCategoryRepository.delete(category);
//        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, null);
//    }
//}