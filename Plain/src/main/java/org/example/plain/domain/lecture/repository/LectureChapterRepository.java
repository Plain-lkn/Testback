package org.example.plain.domain.lecture.repository;

import org.example.plain.domain.lecture.entity.lecture_curriculums.LectureChapterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LectureChapterRepository extends JpaRepository<LectureChapterEntity, String> {
    @Query("SELECT c FROM LectureChapterEntity c LEFT JOIN FETCH c.curriculums WHERE c.unit.id = :unitId ORDER BY c.sequence")
    List<LectureChapterEntity> findByUnitIdWithCurriculums(String unitId);
    
    List<LectureChapterEntity> findByUnitIdOrderBySequence(String unitId);
} 