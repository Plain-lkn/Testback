//package org.example.plain.domain.lecture.repository;
//
//import org.example.plain.domain.lecture.entity.LectureCategoryEntity;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Collection;
//
//public interface LectureCategoryRepository extends JpaRepository<LectureCategoryEntity, String> {
//    boolean existsByName(String name);
//
//    Collection<LectureCategoryEntity> findByName(String categoryId);
//}