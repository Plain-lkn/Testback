package org.example.plain.domain.lecture.service.interfaces;

import org.example.plain.common.ResponseField;
import org.example.plain.domain.lecture.dto.LectureFileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface LectureFileService {
    ResponseField<String> uploadLectureFile(String lectureId, MultipartFile file) throws IOException;
    ResponseField<String> getLectureFile(String lectureId, String fileId);
    ResponseField<List<LectureFileResponse>> getLectureFiles(String lectureId);
    ResponseField<Void> deleteLectureFile(String lectureId, String fileId);
    ResponseField<String> getLectureVideo(String lectureId, String userId, String videoId);
}