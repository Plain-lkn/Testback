package org.example.plain.domain.lecture.repository;

import org.example.plain.domain.lecture.entity.member.LectureMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LectureMemberRepository extends JpaRepository<LectureMemberEntity, String> {
    List<LectureMemberEntity> findByLectureId(String lectureId);
    List<LectureMemberEntity> findByUserId(String userId);
    Optional<LectureMemberEntity> findByLectureIdAndUserId(String lectureId, String userId);
    boolean existsByLectureIdAndUserId(String lectureId, String userId);
} 