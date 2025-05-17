package org.example.plain.domain.lecture.entity.lecture_curriculums;

import jakarta.persistence.*;
import lombok.*;
import org.example.plain.domain.lecture.entity.LectureEntity;

@Entity
@Table(name = "lecture_curriculum")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureCurriculumEntity {
    
    @Id
    @Column(name = "curriculum_id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private LectureChapterEntity chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private LectureEntity lecture;

    @Column(name = "title")
    private String title;

    @Column(name = "sequence")
    private Integer sequence;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(cascade = CascadeType.ALL)
    private LectureVideoCurriculumEntity video;

    public static LectureCurriculumEntity create(LectureEntity lecture, String title, Integer sequence, String description) {
        return LectureCurriculumEntity.builder()
                .id(java.util.UUID.randomUUID().toString())
                .lecture(lecture)
                .title(title)
                .sequence(sequence)
                .description(description)
                .build();
    }

    public void setChapter(LectureChapterEntity chapter) {
        this.chapter = chapter;
    }

    public void setVideo(LectureVideoCurriculumEntity video) {
        this.video = video;
        if (video != null) {
            video.setCurriculum(this);
        }
    }
} 