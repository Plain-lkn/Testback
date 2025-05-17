package org.example.plain.domain.lecture.repository;

import org.example.plain.domain.lecture.entity.lecture_curriculums.LectureCurriculumEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LectureCurriculumRepository extends JpaRepository<LectureCurriculumEntity, String> {
    @Query("SELECT c FROM LectureCurriculumEntity c LEFT JOIN FETCH c.video WHERE c.id = :id")
    Optional<LectureCurriculumEntity> findByIdWithVideo(String id);

    @Query("SELECT c FROM LectureCurriculumEntity c LEFT JOIN FETCH c.video WHERE c.chapter.id = :chapterId ORDER BY c.sequence")
    List<LectureCurriculumEntity> findByChapterIdWithVideos(String chapterId);
    
    List<LectureCurriculumEntity> findByChapterIdOrderBySequence(String chapterId);
} 