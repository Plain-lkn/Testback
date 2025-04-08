package org.example.plain.domain.classMember.repository;

import jakarta.transaction.Transactional;
import org.example.plain.domain.classLecture.entity.ClassLecture;
import org.example.plain.domain.classMember.entity.ClassMember;
import org.example.plain.domain.classMember.entity.ClassMemberId;
import org.example.plain.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassMemberRepository extends JpaRepository<ClassMember, ClassMemberId> {
    Optional<List<ClassMember>> findByUser(User userId);
    Optional<List<ClassMember>> findByClassLecture(ClassLecture lecture);
    Optional<ClassMember> findByClassLectureAndUser(ClassLecture group, User user);
    
    @Query("SELECT cm FROM ClassMember cm WHERE cm.classLecture.id = :classId AND cm.user.id = :userId")
    Optional<ClassMember> findByClassLectureIdAndUserId(@Param("classId") String classId, @Param("userId") String userId);
    
    @Transactional
    void deleteByClassLectureAndUser(ClassLecture group, User user);
}
