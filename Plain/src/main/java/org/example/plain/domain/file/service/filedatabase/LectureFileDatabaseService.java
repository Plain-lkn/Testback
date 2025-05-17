package org.example.plain.domain.file.service.filedatabase;

import org.example.plain.domain.file.dto.FileData;
import org.example.plain.domain.file.dto.LectureFileData;
import org.example.plain.domain.file.entity.FileEntity;
import org.example.plain.domain.file.interfaces.FileDatabaseService;
import org.example.plain.domain.lecture.entity.LectureFileEntity;
import org.example.plain.domain.lecture.repository.LectureFileRepository;
import org.example.plain.domain.lecture.repository.LectureRepository;
import org.springframework.stereotype.Service;

@Service("lectureFileDatabaseService")
public class LectureFileDatabaseService implements FileDatabaseService {

    private final LectureFileRepository lectureFileRepository;
    private final LectureRepository lectureRepository;

    public LectureFileDatabaseService(LectureFileRepository lectureFileRepository, LectureRepository lectureRepository) {
        this.lectureFileRepository = lectureFileRepository;
        this.lectureRepository = lectureRepository;
    }

    @Override
    public FileEntity save(String filename, String filepath, FileData fileData) {
        if (!(fileData instanceof LectureFileData)) {
            throw new IllegalArgumentException("Invalid file data type");
        }

        LectureFileData lectureFileData = (LectureFileData) fileData;
        LectureFileEntity lectureFile = LectureFileEntity.create(
                lectureRepository.findById(lectureFileData.getLectureId())
                        .orElseThrow(() -> new IllegalArgumentException("Lecture not found")),
                filename,
                filepath
        );

        return lectureFileRepository.save(lectureFile);
    }

    @Override
    public void delete(String filename, String filepath) {
        lectureFileRepository.findByFilenameAndFilePath(filename, filepath)
                .ifPresent(lectureFileRepository::delete);
    }

    @Override
    public void chackFileData(FileData fileData) {
        if (!(fileData instanceof LectureFileData)) {
            throw new IllegalArgumentException("Invalid file data type");
        }
        LectureFileData lectureFileData = (LectureFileData) fileData;
        if (lectureFileData.getLectureId() == null) {
            throw new IllegalArgumentException("Lecture ID is required");
        }
    }

    @Override
    public FileEntity findByFilepath(String filepath) {
        return null;
    }

    @Override
    public void delete(FileEntity file) {
        if (file instanceof LectureFileEntity) {
            lectureFileRepository.delete((LectureFileEntity) file);
        }
    }
}
