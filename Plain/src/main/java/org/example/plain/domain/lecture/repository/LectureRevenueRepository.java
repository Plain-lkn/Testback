package org.example.plain.domain.lecture.repository;

import org.example.plain.domain.lecture.entity.LectureRevenueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LectureRevenueRepository extends JpaRepository<LectureRevenueEntity, String> {
    @Query("SELECT SUM(lr.price) FROM LectureRevenueEntity lr WHERE lr.lecture.id = :lectureId")
    Integer calculateTotalRevenueByLectureId(@Param("lectureId") String lectureId);
} 