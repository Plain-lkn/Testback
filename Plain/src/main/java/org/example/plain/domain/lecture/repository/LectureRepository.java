package org.example.plain.domain.lecture.repository;

import org.example.plain.domain.lecture.entity.LectureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LectureRepository extends JpaRepository<LectureEntity, String> {
    List<LectureEntity> findByUserId(String userId);

}
