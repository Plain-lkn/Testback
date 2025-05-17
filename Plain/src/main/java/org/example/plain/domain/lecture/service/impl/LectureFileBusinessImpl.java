package org.example.plain.domain.lecture.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.plain.common.ResponseField;
import org.example.plain.common.enums.Message;
import org.example.plain.domain.file.dto.FileInfo;
import org.example.plain.domain.file.dto.LectureFileData;
import org.example.plain.domain.file.dto.LectureVideoFileData;
import org.example.plain.domain.file.interfaces.CloudFileService;
import org.example.plain.domain.file.interfaces.FileDatabaseService;
import org.example.plain.domain.lecture.dto.LectureFileResponse;
import org.example.plain.domain.lecture.entity.LectureEntity;
import org.example.plain.domain.lecture.entity.LectureFileEntity;
import org.example.plain.domain.lecture.repository.LectureFileRepository;
import org.example.plain.domain.lecture.repository.LectureRepository;
import org.example.plain.domain.lecture.service.interfaces.LectureFileService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LectureFileBusinessImpl implements LectureFileService {

    private final CloudFileService cloudFileService;
    private final FileDatabaseService fileDatabaseService;
    private final LectureRepository lectureRepository;
    private final LectureFileRepository lectureFileRepository;

    public LectureFileBusinessImpl(
            CloudFileService cloudFileService,
            @Qualifier("lectureFileDatabaseService")
            FileDatabaseService fileDatabaseService,
            LectureRepository lectureRepository,
            LectureFileRepository lectureFileRepository) {
        this.cloudFileService = cloudFileService;
        this.fileDatabaseService = fileDatabaseService;
        this.lectureRepository = lectureRepository;
        this.lectureFileRepository = lectureFileRepository;
    }

    @Override
    @Transactional
    public ResponseField<String> uploadLectureFile(String lectureId, MultipartFile file) throws IOException {
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        // 파일 데이터 생성
        LectureFileData fileData = LectureFileData.builder()
                .file(file)
                .lectureId(lectureId)
                .build();

        // S3에 파일 업로드
        FileInfo fileInfo = cloudFileService.uploadSingleFile(fileData, lectureId);
        
        // 데이터베이스에 파일 정보 저장
        fileDatabaseService.save(fileInfo.getFilename(), fileInfo.getFileUrl(), fileData);
        
        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, fileInfo.getFileUrl());
    }

    @Override
    public ResponseField<String> getLectureFile(String lectureId, String fileId) {
        LectureFileEntity lectureFile = lectureFileRepository.findByIdAndLectureId(fileId, lectureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파일입니다."));

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, lectureFile.getFilePath());
    }

    @Override
    public ResponseField<List<LectureFileResponse>> getLectureFiles(String lectureId) {
        List<LectureFileResponse> files = lectureFileRepository.findByLectureId(lectureId)
                .stream()
                .map(LectureFileResponse::from)
                .collect(Collectors.toList());

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, files);
    }

    @Override
    @Transactional
    public ResponseField<Void> deleteLectureFile(String lectureId, String fileId) {
        LectureFileEntity lectureFile = lectureFileRepository.findByIdAndLectureId(fileId, lectureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파일입니다."));

        // S3에서 파일 삭제
        cloudFileService.deleteFile(lectureFile);
        
        // 데이터베이스에서 파일 정보 삭제
        fileDatabaseService.delete(lectureFile);
        
        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, null);
    }

    @Override
    public ResponseField<String> getLectureVideo(String lectureId, String userId, String videoId) {
        LectureFileEntity video = lectureFileRepository.findByIdAndLectureId(videoId, lectureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영상입니다."));

        // TODO: 권한 체크 로직 구현
        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, video.getFilePath());
    }


} 