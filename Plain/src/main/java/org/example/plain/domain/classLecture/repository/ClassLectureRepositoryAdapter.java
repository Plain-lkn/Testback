package org.example.plain.domain.classLecture.repository;

import lombok.RequiredArgsConstructor;
import org.example.plain.domain.classLecture.dto.ClassResponse;
import org.example.plain.domain.classLecture.entity.ClassLecture;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ClassLectureRepositoryAdapter implements ClassLectureRepositoryPort {

    private final ClassLectureRepository classLectureRepository;

    @Override
    public ClassLecture save(ClassLecture classLecture) {
        return classLectureRepository.save(classLecture);
    }

    @Override
    public ClassLecture findById(String id) {
        return classLectureRepository.findById(id)
                .orElseThrow();
    }

    @Override
    public List<ClassLecture> findAll() {
        return classLectureRepository.findAll();
    }

    @Override
    public void delete(String id) {
        ClassLecture classLecture = findById(id);
        classLectureRepository.delete(classLecture);
    }

    @Override
    public ClassLecture findByCode(String code) {
        return classLectureRepository.findByCode(code);
    }

    @Override
    public List<ClassLecture> findByTitle(String query) {
        return classLectureRepository.findByTitle(query);
    }
}
