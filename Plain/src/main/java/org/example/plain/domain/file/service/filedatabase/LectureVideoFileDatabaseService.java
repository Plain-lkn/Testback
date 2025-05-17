package org.example.plain.domain.file.service.filedatabase;

import org.example.plain.domain.file.dto.FileData;
import org.example.plain.domain.file.dto.LectureVideoFileData;
import org.example.plain.domain.file.entity.FileEntity;
import org.example.plain.domain.file.interfaces.FileDatabaseService;
import org.example.plain.domain.lecture.entity.lecture_curriculums.LectureVideoCurriculumEntity;
import org.example.plain.domain.lecture.repository.LectureCurriculumRepository;
import org.example.plain.domain.lecture.repository.LectureVideoCurriculumRepository;
import org.springframework.stereotype.Service;

@Service("lectureVideoFileDatabaseService")
public class LectureVideoFileDatabaseService implements FileDatabaseService {

    private final LectureVideoCurriculumRepository lectureVideoCurriculumRepository;
    private final LectureCurriculumRepository lectureCurriculumRepository;

    public LectureVideoFileDatabaseService(LectureVideoCurriculumRepository lectureVideoCurriculumRepository, LectureCurriculumRepository lectureCurriculumRepository) {
        this.lectureVideoCurriculumRepository = lectureVideoCurriculumRepository;
        this.lectureCurriculumRepository = lectureCurriculumRepository;
    }

    @Override
    public FileEntity save(String filename, String filepath, FileData fileData) {
        if (!(fileData instanceof LectureVideoFileData)) {
            throw new IllegalArgumentException("Invalid file data type");
        }

        LectureVideoFileData videoFileData = (LectureVideoFileData) fileData;
        LectureVideoCurriculumEntity videoCurriculum = LectureVideoCurriculumEntity.builder()
                .curriculum(lectureCurriculumRepository.findById(videoFileData.getCurriculumId()).orElseThrow(IllegalArgumentException::new))
                .playTime(videoFileData.getPlayTime())
                .videoType(videoFileData.getVideoType())
                .filePath(filepath)
                .filename(filename)
                .build();

        return lectureVideoCurriculumRepository.save(videoCurriculum);
    }

    @Override
    public void delete(String filename, String filepath) {
        lectureVideoCurriculumRepository.findByFilePath(filepath)
                .ifPresent(lectureVideoCurriculumRepository::delete);
    }

    @Override
    public void chackFileData(FileData fileData) {
        if (!(fileData instanceof LectureVideoFileData)) {
            throw new IllegalArgumentException("Invalid file data type");
        }
        LectureVideoFileData videoFileData = (LectureVideoFileData) fileData;
        if (videoFileData.getLectureId() == null || videoFileData.getCurriculumId() == null) {
            throw new IllegalArgumentException("Lecture ID and Curriculum ID are required");
        }
        if (videoFileData.getPlayTime() == null || videoFileData.getVideoType() == null) {
            throw new IllegalArgumentException("Play time and video type are required");
        }
    }

    @Override
    public FileEntity findByFilepath(String filepath) {
        return lectureVideoCurriculumRepository.findByFilePath(filepath).orElseThrow(
                () -> new IllegalArgumentException("File not found")
        );
    }

    @Override
    public void delete(FileEntity file) {
        if (file instanceof LectureVideoCurriculumEntity) {
            lectureVideoCurriculumRepository.delete((LectureVideoCurriculumEntity) file);
        }
    }
} 