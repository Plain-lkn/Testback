package org.example.plain.domain.file.interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface FileService {
    File getFile(String filename);
    List<File> getFiles(String workId, String userId);
    File saveFile(MultipartFile file, String filename);
    void deleteFile(String filename);
    String makeFilename(String originalFilename, String userId);
}
