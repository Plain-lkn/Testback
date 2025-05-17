package org.example.plain.domain.lecture.entity.lecture_curriculums;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.example.plain.domain.file.entity.FileEntity;

@Entity
@Table(name = "lecture_video_curriculum")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class LectureVideoCurriculumEntity extends FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String videoCurriculumId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_id")
    private LectureCurriculumEntity curriculum;

    private Integer playTime;
    private String videoType;

    public void setCurriculum(LectureCurriculumEntity curriculum) {
        this.curriculum = curriculum;
    }

    public static LectureVideoCurriculumEntity create(String filename, String filepath, Integer playTime, String videoType) {
        return LectureVideoCurriculumEntity.builder()
                .videoCurriculumId(java.util.UUID.randomUUID().toString())
                .filename(filename)
                .filePath(filepath)
                .playTime(playTime)
                .videoType(videoType)
                .build();
    }
} 