package org.example.plain.domain.file.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.plain.domain.user.entity.User;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class FileEntity {
    private String filename;

    @Column(name = "file_path")
    private String filePath;


    public static List<FileEntity> fileEntities(List<File> files) {
        List<FileEntity> fileEntities = new ArrayList<>();
        return fileEntities;
    }
}

