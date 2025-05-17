package org.example.plain.domain.lecture.repository;

import org.example.plain.domain.lecture.entity.LectureFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LectureFileRepository extends JpaRepository<LectureFileEntity, String> {
    List<LectureFileEntity> findByLectureId(String lectureId);
    Optional<LectureFileEntity> findByIdAndLectureId(String id, String lectureId);
    Optional<LectureFileEntity> findByFilenameAndFilePath(String filename, String filepath);
}