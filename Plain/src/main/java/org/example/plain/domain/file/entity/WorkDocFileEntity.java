package org.example.plain.domain.file.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.plain.domain.file.entity.id.WorkDocFileKey;
import org.example.plain.domain.homework.entity.WorkEntity;
import org.example.plain.domain.homework.entity.WorkMemberEntity;

@Data
@Entity
@Table(name = "work_doc_file")
@NoArgsConstructor
public class WorkDocFileEntity extends FileEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "h_id", referencedColumnName = "h_id")
    private WorkEntity work;

    @Builder
    public WorkDocFileEntity(String filename, String filePath, WorkEntity work) {
        super(filename, filePath);
        this.work = work;
    }
}
