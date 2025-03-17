package org.example.plain.domain.classLecture.repository;

import org.example.plain.domain.classLecture.dto.ClassResponse;
import org.example.plain.domain.classLecture.entity.ClassLecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassLectureRepository extends JpaRepository<ClassLecture, String> {

    ClassLecture findByCode(String code);

    List<ClassResponse> findByTitle(String query);
}
