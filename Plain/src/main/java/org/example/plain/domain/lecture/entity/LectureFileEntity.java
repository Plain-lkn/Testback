package org.example.plain.domain.lecture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.example.plain.domain.file.entity.FileEntity;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LectureFileEntity extends FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "lecture_id")
    private LectureEntity lecture;

    public static LectureFileEntity create(LectureEntity lecture, String filename, String filepath) {
        return LectureFileEntity.builder()
                .lecture(lecture)
                .filename(filename)
                .filePath(filepath)
                .build();
    }
} 