package org.example.plain.domain.file.interfaces;

import org.example.plain.domain.file.dto.FileData;
import org.example.plain.domain.file.entity.FileEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CloudFileService {
    public FileEntity uploadSingleFile(FileData fileData);
    public List<FileEntity> uploadFiles(FileData fileData, List<MultipartFile> files);
    public void deleteFile(String filename);
    public String makeFilename(String originalFilename, String userId);
}
