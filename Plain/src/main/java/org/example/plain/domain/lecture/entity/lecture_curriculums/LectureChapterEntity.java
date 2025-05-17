package org.example.plain.domain.lecture.entity.lecture_curriculums;

import jakarta.persistence.*;
import lombok.*;
import org.example.plain.domain.lecture.entity.LectureEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lecture_chapter")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureChapterEntity {
    
    @Id
    @Column(name = "chapter_id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private LectureUnitEntity unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private LectureEntity lecture;

    @Column(name = "title")
    private String title;

    @Column(name = "sequence")
    private Integer sequence;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence asc")
    private List<LectureCurriculumEntity> curriculums = new ArrayList<>();

    public static LectureChapterEntity create(LectureEntity lecture, String title, Integer sequence) {
        return LectureChapterEntity.builder()
                .id(java.util.UUID.randomUUID().toString())
                .lecture(lecture)
                .title(title)
                .sequence(sequence)
                .build();
    }

    public void setUnit(LectureUnitEntity unit) {
        this.unit = unit;
    }

    public void addCurriculum(LectureCurriculumEntity curriculum) {
        this.curriculums.add(curriculum);
        curriculum.setChapter(this);
    }

    public void removeCurriculum(LectureCurriculumEntity curriculum) {
        this.curriculums.remove(curriculum);
        curriculum.setChapter(null);
    }
} 