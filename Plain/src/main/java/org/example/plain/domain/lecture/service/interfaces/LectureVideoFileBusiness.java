package org.example.plain.domain.lecture.service.interfaces;

import org.example.plain.common.ResponseField;
import org.example.plain.domain.lecture.dto.lecturecurriculum.LectureVideoResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface LectureVideoFileBusiness {
    ResponseField<String> uploadLectureVideo(String lectureId, String curriculumId, MultipartFile file, Integer playTime, String videoType) throws IOException;
    ResponseField<Void> deleteLectureVideo(String lectureId, String curriculumId, String videoId);
    ResponseField<LectureVideoResponse> getLectureVideo(String lectureId, String curriculumId, String videoId);
    ResponseField<List<LectureVideoResponse>> getLectureVideos(String lectureId, String curriculumId);
}
