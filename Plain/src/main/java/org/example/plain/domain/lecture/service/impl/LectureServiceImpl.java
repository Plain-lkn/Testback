package org.example.plain.domain.lecture.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.plain.common.ResponseField;
import org.example.plain.common.enums.Message;
import org.example.plain.domain.lecture.dto.LectureRequest;
import org.example.plain.domain.lecture.dto.LectureResponse;
import org.example.plain.domain.lecture.entity.LectureEntity;
import org.example.plain.domain.lecture.repository.LectureRepository;
import org.example.plain.domain.lecture.service.interfaces.LectureService;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureServiceImpl implements LectureService {

    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;

    @Override
    public ResponseField<List<LectureResponse>> getLectureList() {
        List<LectureEntity> lectures = lectureRepository.findAll();

        if (lectures.isEmpty()) {
            throw new NoSuchElementException("등록된 강의가 없습니다.");
        }

        List<LectureResponse> responses = lectures.stream()
                .map(LectureResponse::from)
                .collect(Collectors.toList());

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, responses);
    }

    @Override
    public ResponseField<LectureResponse> getLectureDetailShop(String lectureId) {
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, LectureResponse.from(lecture));
    }

    @Override
    public ResponseField<LectureResponse> getLectureDetail(String lectureId) {
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, LectureResponse.from(lecture));
    }

    @Override
    @Transactional
    public ResponseField<LectureResponse> createLecture(LectureRequest request, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        LectureEntity lecture = LectureEntity.create(
                user,
                request.getLectureType(),
                request.getLectureName(),
                request.getLectureDescription(),
                request.getLecturePrice());

        LectureEntity savedLecture = lectureRepository.save(lecture);
        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, LectureResponse.from(savedLecture));
    }

    @Override
    @Transactional
    public ResponseField<LectureResponse> updateLecture(String lectureId, LectureRequest request, String userId) {
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        if (!lecture.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        lecture.update(
                request.getLectureType(),
                request.getLectureName(),
                request.getLectureDescription(),
                request.getLecturePrice());

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, LectureResponse.from(lecture));
    }

    @Override
    @Transactional
    public void deleteLecture(String lectureId, String userId) {
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        if (!lecture.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        lectureRepository.delete(lecture);
    }
} 