package org.example.plain.domain.lecture.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "lecture_category")
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LectureCategoryEntity {

    @Id
    @Column(name = "category_id")
    private String id;

    @Column(name = "category", columnDefinition = "TINYTEXT")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private LectureEntity lecture;

    public static LectureCategoryEntity create(String name) {
        return LectureCategoryEntity.builder()
                .id(java.util.UUID.randomUUID().toString())
                .name(name)
                .build();
    }
} 