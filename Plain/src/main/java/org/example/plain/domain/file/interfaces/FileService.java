package org.example.plain.domain.file.interfaces;

import org.example.plain.domain.file.entity.FileEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface FileService {
    // 일반 파일 업로드 (참조 타입, 참조 ID 기반)
    FileEntity uploadFile(MultipartFile file, String referenceType, String referenceId, String userId);
    
    // 여러 파일 업로드
    List<FileEntity> uploadFiles(List<MultipartFile> files, String referenceType, String referenceId, String userId);
    
    // 파일 조회
    File getFile(String fileId);
    
    // 참조 타입과 ID로 파일 목록 조회
    List<FileEntity> getFilesByReference(String referenceType, String referenceId);
    
    // 파일 삭제
    void deleteFile(String fileId);
    
    // 참조 타입과 ID로 파일 일괄 삭제
    void deleteFilesByReference(String referenceType, String referenceId);
    
    // 파일명 생성 헬퍼 메소드
    String makeFilename(String originalFilename, String userId);
}
