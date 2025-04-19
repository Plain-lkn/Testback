package org.example.plain.domain.file.service;

import lombok.RequiredArgsConstructor;
import org.example.plain.domain.file.dto.FileData;
import org.example.plain.domain.file.dto.SubmitFileData;
import org.example.plain.domain.file.entity.FileEntity;
import org.example.plain.domain.file.entity.WorkFileEntity;
import org.example.plain.domain.file.interfaces.FileDatabaseService;
import org.example.plain.domain.homework.entity.WorkMemberEntity;
import org.example.plain.domain.file.repository.FileRepository;
import org.example.plain.repository.WorkMemberRepository;
import org.springframework.data.relational.core.sql.In;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service(value = "assignmentDatabase")
@RequiredArgsConstructor
public class AssignmentFileDatabaseServiceImpl implements FileDatabaseService {

    private final FileRepository fileRepository;
    private final WorkMemberRepository workMemberRepository;

    /**
     * 과제 제출 파일 저장
     * 이미 동일한 파일명이 존재하면 기존 파일 정보 반환
     * @param filename 파일명
     * @param filepath 파일 경로
     * @param fileData 파일 데이터
     * @return 파일 엔티티
     */
    @Override
    @Transactional
    public FileEntity save(String filename, String filepath, FileData fileData) {
        SubmitFileData submitFileData = fileData instanceof SubmitFileData ? (SubmitFileData) fileData : null;

        if (submitFileData.getUserId() == null || submitFileData.getWorkId() == null) {
            throw new IllegalArgumentException("User ID and Work ID cannot be null");
        }

        WorkMemberEntity workMember = workMemberRepository.findByWorkIdAndUserId(
                submitFileData.getWorkId().getWorkId(),
                submitFileData.getUserId().getId()
        ).orElseThrow(() -> new IllegalArgumentException("WorkMember not found"));

        // 이미 동일한 파일명이 존재하는지 확인
        Optional<WorkFileEntity> existingFile = fileRepository.findByFilenameAndWorkMember(
                filename, workMember);
        
        if (existingFile.isPresent()) {
            // 이미 존재하는 파일이면 기존 파일 정보 반환
            return existingFile.get();
        }

        return fileRepository.save(
                WorkFileEntity.builder()
                        .filename(filename)
                        .filePath(filepath)
                        .workMember(workMember)
                        .build()
        );
    }

    @Override
    public void chackFileData(FileData fileData) {
        SubmitFileData submitFileData = fileData instanceof SubmitFileData ? (SubmitFileData) fileData : null;

        if (submitFileData.getUserId() == null || submitFileData.getWorkId() == null) {
            throw new IllegalArgumentException("User ID and Work ID cannot be null");
        }
    }

    @Override
    @Transactional
    public void delete(FileEntity file) {
        fileRepository.delete((WorkFileEntity) file);
    }

    @Override
    public void delete(String filename, String filepath) {
        // 파일명과 경로로 파일 찾기
        Optional<WorkFileEntity> file = fileRepository.findByFilenameAndFilePath(filename, filepath);
        file.ifPresent(fileRepository::delete);
    }

    /**
     * 특정 과제와 사용자에 대해 파일이 이미 존재하는지 확인
     * @param filename 파일명
     * @param workId 과제 ID
     * @param userId 사용자 ID
     * @return 존재하면 true, 아니면 false
     */
    @Transactional(readOnly = true)
    public boolean isFileExists(String filename, String workId, String userId) {
        WorkMemberEntity workMember = workMemberRepository.findByWorkIdAndUserId(
                workId, userId
        ).orElse(null);
        
        if (workMember == null) {
            return false;
        }
        
        return fileRepository.findByFilenameAndWorkMember(filename, workMember).isPresent();
    }
}
