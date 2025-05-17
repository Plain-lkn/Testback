package org.example.plain.domain.lecture.entity.lecture_curriculums;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.plain.domain.lecture.entity.LectureEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lecture_unit")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureUnitEntity {
    @Id
    @Column(name = "unit_id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private LectureEntity lecture;

    private String title;
    private Integer sequence;

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence asc")
    private List<LectureChapterEntity> chapters = new ArrayList<>();

    public static LectureUnitEntity create(LectureEntity lecture, String title, Integer sequence) {
        return LectureUnitEntity.builder()
                .id(java.util.UUID.randomUUID().toString())
                .lecture(lecture)
                .title(title)
                .sequence(sequence)
                .build();
    }

    public void addChapter(LectureChapterEntity chapter) {
        this.chapters.add(chapter);
        chapter.setUnit(this);
    }

    public void removeChapter(LectureChapterEntity chapter) {
        this.chapters.remove(chapter);
        chapter.setUnit(null);
    }
} 