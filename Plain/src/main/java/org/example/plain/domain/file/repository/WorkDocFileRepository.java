package org.example.plain.domain.file.repository;

import org.example.plain.domain.file.entity.WorkDocFileEntity;
import org.example.plain.domain.file.entity.id.WorkDocFileKey;
import org.example.plain.domain.homework.entity.WorkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkDocFileRepository extends JpaRepository<WorkDocFileEntity, WorkDocFileKey> {
    // 과제와 연결된 모든 파일 조회
    List<WorkDocFileEntity> findByWork(WorkEntity work);
    
    // 과제 ID로 파일 목록 조회
    List<WorkDocFileEntity> findByWorkWorkId(String workId);
    
    // 파일명과 경로로 파일 조회
    Optional<WorkDocFileEntity> findByFilenameAndFilePath(String filename, String filePath);
    
    // 파일명과 과제 ID로 파일 조회
    Optional<WorkDocFileEntity> findByFilenameAndWorkWorkId(String filename, String workId);
    
    // 과제 ID로 파일 삭제
    void deleteByWorkWorkId(String workId);
}
