package org.example.plain.domain.lecture.service.impl;

import org.example.plain.common.ResponseField;
import org.example.plain.common.enums.Message;
import org.example.plain.domain.file.dto.FileInfo;
import org.example.plain.domain.file.dto.LectureVideoFileData;
import org.example.plain.domain.file.interfaces.CloudFileService;
import org.example.plain.domain.file.interfaces.FileDatabaseService;
import org.example.plain.domain.lecture.dto.lecturecurriculum.LectureVideoResponse;
import org.example.plain.domain.lecture.entity.LectureEntity;
import org.example.plain.domain.lecture.entity.lecture_curriculums.LectureVideoCurriculumEntity;
import org.example.plain.domain.lecture.repository.LectureRepository;
import org.example.plain.domain.lecture.repository.LectureVideoCurriculumRepository;
import org.example.plain.domain.lecture.service.interfaces.LectureVideoFileBusiness;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LectureVideoFileBusinessImpl implements LectureVideoFileBusiness {

    private final LectureRepository lectureRepository;
    private final CloudFileService cloudFileService;
    private final FileDatabaseService fileDatabaseService;
    private final LectureVideoCurriculumRepository lectureVideoCurriculumRepository;

    public LectureVideoFileBusinessImpl(
            LectureRepository lectureRepository,
            CloudFileService cloudFileService,
            @Qualifier("lectureVideoFileDatabaseService")
            FileDatabaseService fileDatabaseService,
            LectureVideoCurriculumRepository lectureVideoCurriculumRepository) {
        this.lectureRepository = lectureRepository;
        this.cloudFileService = cloudFileService;
        this.fileDatabaseService = fileDatabaseService;
        this.lectureVideoCurriculumRepository = lectureVideoCurriculumRepository;
    }

    @Override
    @Transactional
    public ResponseField<String> uploadLectureVideo(String lectureId, String curriculumId, MultipartFile file, Integer playTime, String videoType) throws IOException {
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        // 비디오 파일 데이터 생성
        LectureVideoFileData videoData = LectureVideoFileData.builder()
                .file(file)
                .lectureId(lectureId)
                .curriculumId(curriculumId)
                .playTime(playTime)
                .videoType(videoType)
                .build();

        // S3에 비디오 업로드
        FileInfo fileInfo = cloudFileService.uploadSingleFile(videoData, lectureId, curriculumId);

        // 데이터베이스에 비디오 정보 저장
        fileDatabaseService.save(fileInfo.getFilename(), fileInfo.getFileUrl(), videoData);

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, fileInfo.getFileUrl());
    }

    @Override
    @Transactional
    public ResponseField<Void> deleteLectureVideo(String lectureId, String curriculumId, String videoId) {
        LectureVideoCurriculumEntity video = lectureVideoCurriculumRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영상입니다."));

        // S3에서 파일 삭제
        cloudFileService.deleteFile(video);

        // 데이터베이스에서 파일 정보 삭제
        fileDatabaseService.delete(video);

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, null);
    }

    @Override
    public ResponseField<LectureVideoResponse> getLectureVideo(String lectureId, String curriculumId, String videoId) {
        LectureVideoCurriculumEntity video = lectureVideoCurriculumRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영상입니다."));

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, LectureVideoResponse.from(video));
    }

    @Override
    public ResponseField<List<LectureVideoResponse>> getLectureVideos(String lectureId, String curriculumId) {
        List<LectureVideoResponse> videos = lectureVideoCurriculumRepository.findAll()
                .stream()
                .map(LectureVideoResponse::from)
                .collect(Collectors.toList());

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, videos);
    }
}
