package org.example.plain.domain.lecture.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.plain.common.ResponseField;
import org.example.plain.common.enums.Message;
import org.example.plain.domain.lecture.dto.lecturecurriculum.*;
import org.example.plain.domain.lecture.entity.LectureEntity;
import org.example.plain.domain.lecture.entity.lecture_curriculums.*;
import org.example.plain.domain.lecture.repository.*;
import org.example.plain.domain.lecture.service.interfaces.LectureCurriculumService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureCurriculumServiceImpl implements LectureCurriculumService {

    private final LectureRepository lectureRepository;
    private final LectureUnitRepository unitRepository;
    private final LectureChapterRepository chapterRepository;
    private final LectureCurriculumRepository curriculumRepository;

    @Override
    @Transactional
    public ResponseField<LectureUnitResponse> createUnit(String lectureId, LectureUnitRequest request) {
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        LectureUnitEntity unit = LectureUnitEntity.create(lecture, request.getTitle(), request.getSequence());
        unit = unitRepository.save(unit);

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, LectureUnitResponse.from(unit));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseField<List<LectureUnitResponse>> getUnits(String lectureId) {
        List<LectureUnitEntity> units = unitRepository.findByLectureIdOrderBySequence(lectureId);
        List<LectureUnitResponse> responses = units.stream()
                .map(LectureUnitResponse::from)
                .collect(Collectors.toList());

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, responses);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseField<LectureUnitResponse> getUnit(String unitId) {
        LectureUnitEntity unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 단원입니다."));

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, LectureUnitResponse.from(unit));
    }

    @Override
    @Transactional
    public ResponseField<Void> deleteUnit(String unitId) {
        unitRepository.deleteById(unitId);
        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, null);
    }

    @Override
    @Transactional
    public ResponseField<LectureChapterResponse> createChapter(String unitId, LectureChapterRequest request) {
        LectureUnitEntity unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 단원입니다."));

        LectureChapterEntity chapter = LectureChapterEntity.create(unit.getLecture(), request.getTitle(), request.getSequence());
        unit.addChapter(chapter);
        chapter = chapterRepository.save(chapter);

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, LectureChapterResponse.from(chapter));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseField<List<LectureChapterResponse>> getChapters(String unitId) {
        List<LectureChapterEntity> chapters = chapterRepository.findByUnitIdWithCurriculums(unitId);
        List<LectureChapterResponse> responses = chapters.stream()
                .map(LectureChapterResponse::from)
                .collect(Collectors.toList());

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, responses);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseField<LectureChapterResponse> getChapter(String chapterId) {
        LectureChapterEntity chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 챕터입니다."));

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, LectureChapterResponse.from(chapter));
    }

    @Override
    @Transactional
    public ResponseField<Void> deleteChapter(String chapterId) {
        chapterRepository.deleteById(chapterId);
        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, null);
    }

    @Override
    @Transactional
    public ResponseField<LectureCurriculumResponse> createCurriculum(String chapterId, LectureCurriculumRequest request) {
        LectureChapterEntity chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 챕터입니다."));

        LectureCurriculumEntity curriculum = LectureCurriculumEntity.create(
                chapter.getLecture(),
                request.getTitle(),
                request.getSequence(),
                request.getDescription()
        );
        chapter.addCurriculum(curriculum);
        curriculum = curriculumRepository.save(curriculum);

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, LectureCurriculumResponse.from(curriculum));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseField<List<LectureCurriculumResponse>> getCurriculums(String chapterId) {
        List<LectureCurriculumEntity> curriculums = curriculumRepository.findByChapterIdWithVideos(chapterId);
        List<LectureCurriculumResponse> responses = curriculums.stream()
                .map(LectureCurriculumResponse::from)
                .collect(Collectors.toList());

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, responses);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseField<LectureCurriculumResponse> getCurriculum(String curriculumId) {
        LectureCurriculumEntity curriculum = curriculumRepository.findByIdWithVideo(curriculumId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 커리큘럼입니다."));

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, LectureCurriculumResponse.from(curriculum));
    }

    @Override
    @Transactional
    public ResponseField<Void> deleteCurriculum(String curriculumId) {
        curriculumRepository.deleteById(curriculumId);
        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, null);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseField<List<LectureUnitResponse>> getLectureCurriculum(String lectureId) {
        List<LectureUnitEntity> units = unitRepository.findByLectureIdWithChaptersAndCurriculums(lectureId);
        List<LectureUnitResponse> responses = units.stream()
                .map(LectureUnitResponse::from)
                .collect(Collectors.toList());

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, responses);
    }
} 