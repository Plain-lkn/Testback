package org.example.plain.domain.file.interfaces;

import org.example.plain.domain.file.dto.FileData;
import org.example.plain.domain.file.dto.FileInfo;
import org.example.plain.domain.file.dto.FileServiceGenericInfo;
import org.example.plain.domain.file.entity.FileEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CloudFileService {
    FileInfo uploadSingleFile(FileData fileData, String... id);

    List<FileInfo> uploadFiles(FileData fileData, List<MultipartFile> files, String... id);

    public void deleteFile(FileEntity file);
    public void deleteFile(String filepath);
    public String makeFilename(String originalFilename, String userId);
}
