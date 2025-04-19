package org.example.plain.domain.file.service;

import lombok.RequiredArgsConstructor;
import org.example.plain.domain.file.dto.FileData;
import org.example.plain.domain.file.dto.WorkFileData;
import org.example.plain.domain.file.entity.FileEntity;
import org.example.plain.domain.file.entity.WorkDocFileEntity;
import org.example.plain.domain.file.entity.id.WorkDocFileKey;
import org.example.plain.domain.file.interfaces.CloudFileService;
import org.example.plain.domain.file.interfaces.FileDatabaseService;
import org.example.plain.domain.file.repository.WorkDocFileRepository;
import org.example.plain.domain.homework.entity.WorkEntity;
import org.example.plain.repository.BoardRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service("workDoc")
@RequiredArgsConstructor
public class WorkDocFileDatabaseService implements FileDatabaseService {
    
    private final WorkDocFileRepository workDocFileRepository;
    private final BoardRepository workRepository;
    private final CloudFileService cloudFileService;

    /**
     * 과제 문서 파일 저장
     * 이미 저장된 파일명이 있는 경우 기존 파일 정보 반환
     * @param filename 파일명
     * @param filepath 파일 경로
     * @param fileData 파일 데이터
     * @return 파일 엔티티
     */
    @Override
    @Transactional
    public FileEntity save(String filename, String filepath, FileData fileData) {
        if (!(fileData instanceof WorkFileData)) {
            throw new IllegalArgumentException("파일 데이터는 WorkFileData 타입이어야 합니다.");
        }

        WorkFileData workFileData = (WorkFileData) fileData;
        
        // 1. 과제 엔티티 조회
        WorkEntity workEntity = workRepository.findByWorkId(workFileData.getWorkId())
                .orElseThrow(() -> new RuntimeException("과제를 찾을 수 없습니다. ID: " + workFileData.getWorkId()));
        
        // 1.1 이미 동일한 파일명이 DB에 있는지 확인
        Optional<WorkDocFileEntity> existingFile = workDocFileRepository.findByFilenameAndWorkWorkId(filename, workEntity.getWorkId());
        if (existingFile.isPresent()) {
            // 이미 저장된 파일이 있으면 그 정보 반환
            return existingFile.get();
        }
        
        // 2. 과제 문서 파일 엔티티 생성
        WorkDocFileEntity workDocFileEntity = new WorkDocFileEntity(filename, filepath, workEntity);
        
        // 3. 합성 키 생성 및 저장
//        WorkDocFileKey fileKey = new WorkDocFileKey(null, workEntity.getWorkId());
//        workDocFileEntity.setFileKey(fileKey);
        
        // 4. 저장 후 반환
        return workDocFileRepository.save(workDocFileEntity);
    }

    /**
     * 파일명과 경로로 과제 문서 파일 삭제
     * @param filename 파일명
     * @param filepath 파일 경로
     */
    @Override
    @Transactional
    public void delete(String filename, String filepath) {
        // 파일명과 경로로 파일 찾기 - 업데이트된 메서드 활용
        workDocFileRepository.findByFilenameAndFilePath(filename, filepath)
                .ifPresent(file -> {
                    // 실제 클라우드에서 파일 삭제
                    cloudFileService.deleteFile(filepath);
                    // DB에서 파일 정보 삭제
                    workDocFileRepository.delete(file);
                });
    }

    /**
     * 파일 데이터 유효성 검사
     * @param fileData 파일 데이터
     */
    @Override
    public void chackFileData(FileData fileData) {
        if (fileData == null) {
            throw new IllegalArgumentException("파일 데이터가 null입니다.");
        }
        
        if (fileData.getFile() == null) {
            throw new IllegalArgumentException("파일이 null입니다.");
        }
        
        if (!(fileData instanceof WorkFileData)) {
            throw new IllegalArgumentException("파일 데이터는 WorkFileData 타입이어야 합니다.");
        }
        
        WorkFileData workFileData = (WorkFileData) fileData;
        
        if (workFileData.getWorkId() == null || workFileData.getWorkId().isEmpty()) {
            throw new IllegalArgumentException("과제 ID는 필수 입력값입니다.");
        }
    }

    /**
     * 파일 엔티티로 과제 문서 파일 삭제
     * @param file 파일 엔티티
     */
    @Override
    @Transactional
    public void delete(FileEntity file) {
        if (file instanceof WorkDocFileEntity) {
            WorkDocFileEntity workDocFile = (WorkDocFileEntity) file;
            // 실제 클라우드에서 파일 삭제
            cloudFileService.deleteFile(workDocFile.getFilePath());
            // DB에서 파일 정보 삭제
            workDocFileRepository.delete(workDocFile);
        } else {
            throw new IllegalArgumentException("파일이 WorkDocFileEntity 타입이 아닙니다.");
        }
    }
    
    /**
     * 과제 ID로 모든 과제 문서 파일 삭제
     * @param workId 과제 ID
     */
    @Transactional
    public void deleteAllByWorkId(String workId) {
        // 과제가 존재하는지 확인
        workRepository.findById(workId)
                .orElseThrow(() -> new RuntimeException("과제를 찾을 수 없습니다. ID: " + workId));
        
        // 과제 ID로 연결된 모든 파일 조회
        List<WorkDocFileEntity> files = workDocFileRepository.findByWorkWorkId(workId);
        
        // 각 파일 삭제
        for (WorkDocFileEntity file : files) {
            // 실제 클라우드에서 파일 삭제
            cloudFileService.deleteFile(file.getFilePath());
        }
        
        // DB에서 파일 정보 한 번에 삭제
        workDocFileRepository.deleteByWorkWorkId(workId);
    }
    
    /**
     * 과제 ID로 과제 문서 파일 목록 조회
     * @param workId 과제 ID
     * @return 과제 문서 파일 목록
     */
    public List<WorkDocFileEntity> findAllByWorkId(String workId) {
        // 과제가 존재하는지 확인
        workRepository.findById(workId)
                .orElseThrow(() -> new RuntimeException("과제를 찾을 수 없습니다. ID: " + workId));
        
        // 과제와 연결된 모든 파일 조회 및 반환
        return workDocFileRepository.findByWorkWorkId(workId);
    }
    
    /**
     * 과제 엔티티로 과제 문서 파일 목록 조회
     * @param workEntity 과제 엔티티
     * @return 과제 문서 파일 목록
     */
    public List<WorkDocFileEntity> findAllByWork(WorkEntity workEntity) {
        return workDocFileRepository.findByWork(workEntity);
    }
    
    /**
     * 파일명과 과제 ID로 이미 저장된 파일인지 확인
     * @param filename 파일명
     * @param workId 과제 ID
     * @return 저장되어 있으면 true, 아니면 false
     */
    public boolean isFileExists(String filename, String workId) {
        return workDocFileRepository.findByFilenameAndWorkWorkId(filename, workId).isPresent();
    }
}
