package org.example.plain.domain.file.repository;

import org.example.plain.domain.file.entity.FileEntity;
import org.example.plain.domain.file.entity.WorkFileEntity;
import org.example.plain.domain.homework.entity.WorkMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<WorkFileEntity, Integer> {
    // 파일명과 과제 멤버로 파일 조회
    Optional<WorkFileEntity> findByFilenameAndWorkMember(String filename, WorkMemberEntity workMember);
    
    // 파일명과 파일 경로로 파일 조회
    Optional<WorkFileEntity> findByFilenameAndFilePath(String filename, String filePath);
}
