package org.example.plain.domain.file.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.plain.domain.homework.entity.WorkMemberEntity;
import org.example.plain.domain.user.entity.User;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "file", schema = "plain")
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "h_id", referencedColumnName = "h_id", nullable = false, insertable = false, updatable = false),
        @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false, insertable = false, updatable = false)
    })
    private WorkMemberEntity workMember;

    @Transient
    private User user;

    private String filename;

    @Column(name = "file_path")
    private String filePath;

    @Builder
    public FileEntity(String filename, String filePath, WorkMemberEntity workMember) {
        this.filename = filename;
        this.filePath = filePath;
        this.workMember = workMember;
        if (workMember != null) {
            this.user = workMember.getUser();
        }
    }

    public static List<FileEntity> fileEntities(List<File> files) {
        List<FileEntity> fileEntities = new ArrayList<>();
        for (File file : files) {
            FileEntity fileEntity = new FileEntity();
            fileEntity.setFilename(file.getName());
            fileEntities.add(fileEntity);
        }
        return fileEntities;
    }
}

