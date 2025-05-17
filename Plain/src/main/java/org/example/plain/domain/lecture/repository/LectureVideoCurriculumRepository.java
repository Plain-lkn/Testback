package org.example.plain.domain.lecture.repository;

import org.example.plain.domain.lecture.entity.lecture_curriculums.LectureVideoCurriculumEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LectureVideoCurriculumRepository extends JpaRepository<LectureVideoCurriculumEntity, String> {
    Optional<LectureVideoCurriculumEntity> findByFilePath(String filepath);
} 