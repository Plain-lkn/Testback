package org.example.plain.domain.file.interfaces;

import org.example.plain.domain.file.dto.FileData;
import org.example.plain.domain.file.entity.FileEntity;
import org.example.plain.domain.file.entity.WorkFileEntity;

import java.util.List;

public interface FileDatabaseService {
    FileEntity save(String filename, String filepath, FileData fileData);
    void delete(String filename, String filepath);

    void chackFileData(FileData fileData);

    void delete(FileEntity file);
}
