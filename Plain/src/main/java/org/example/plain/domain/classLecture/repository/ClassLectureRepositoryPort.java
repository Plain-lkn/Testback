package org.example.plain.domain.classLecture.repository;

import org.example.plain.domain.classLecture.dto.ClassResponse;
import org.example.plain.domain.classLecture.entity.ClassLecture;

import java.util.List;

public interface ClassLectureRepositoryPort {
    ClassLecture save(ClassLecture classLecture);

    ClassLecture findById(String id);

    List<ClassLecture> findAll();

    void delete(String id);

    ClassLecture findByCode(String code);

    List<ClassLecture> findByTitle(String query);
}
