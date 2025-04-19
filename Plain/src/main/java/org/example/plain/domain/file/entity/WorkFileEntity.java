package org.example.plain.domain.file.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.plain.domain.homework.entity.WorkMemberEntity;
import org.example.plain.domain.user.entity.User;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "file", schema = "plain")
public class WorkFileEntity extends FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "h_id", referencedColumnName = "h_id"),
            @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    })
    private WorkMemberEntity workMember;

    @Transient
    private User user;

    @Builder
    public WorkFileEntity(String filename, String filePath, WorkMemberEntity workMember) {
        super(filename, filePath);
        this.workMember = workMember;
        if (workMember != null) {
            this.user = workMember.getUser();
        }
    }

    // 정적 팩토리 메소드
    public static WorkFileEntity createWorkFile(String filename, String filePath, WorkMemberEntity workMember) {
        return WorkFileEntity.builder()
                .filename(filename)
                .filePath(filePath)
                .workMember(workMember)
                .build();
    }

    // FileEntity의 정적 메소드 구현
    public static List<FileEntity> workFileEntities(List<File> files, WorkMemberEntity workMember) {
        List<FileEntity> fileEntities = new ArrayList<>();
        for (File file : files) {
            WorkFileEntity fileEntity = new WorkFileEntity();
            fileEntity.setFilename(file.getName());
            fileEntity.setWorkMember(workMember);
            fileEntities.add(fileEntity);
        }
        return fileEntities;
    }
}
