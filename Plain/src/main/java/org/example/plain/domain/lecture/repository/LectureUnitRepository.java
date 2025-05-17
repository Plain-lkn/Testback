package org.example.plain.domain.lecture.repository;

import org.example.plain.domain.lecture.entity.lecture_curriculums.LectureUnitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LectureUnitRepository extends JpaRepository<LectureUnitEntity, String> {
    @Query("SELECT u FROM LectureUnitEntity u LEFT JOIN FETCH u.chapters c LEFT JOIN FETCH c.curriculums WHERE u.lecture.id = :lectureId ORDER BY u.sequence, c.sequence")
    List<LectureUnitEntity> findByLectureIdWithChaptersAndCurriculums(String lectureId);
    
    List<LectureUnitEntity> findByLectureIdOrderBySequence(String lectureId);
} 