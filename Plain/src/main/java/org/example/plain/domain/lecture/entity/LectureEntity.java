package org.example.plain.domain.lecture.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.plain.domain.lecture.enums.LectureType;
import org.example.plain.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lecture")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LectureEntity {

    @Id
    @Column(name = "lecture_id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "lecture_type", length = 10)
    private LectureType lectureType;

    @Column(name = "lecture_name", columnDefinition = "TINYTEXT")
    private String lectureName;

    @Column(name = "lecture_description", columnDefinition = "TEXT")
    private String lectureDescription;

    @Column(name = "lecture_price")
    private Integer lecturePrice;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @PrePersist
    protected void onCreate() {
        createAt = LocalDateTime.now();
        updateAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateAt = LocalDateTime.now();
    }

    // 짧은 ID 생성 메서드
    private static String generateShortId() {
        return "test" + Math.abs(UUID.randomUUID().toString().hashCode() % 10000);
    }

    public static LectureEntity create(User user, LectureType lectureType, String lectureName,
            String lectureDescription, Integer lecturePrice) {
        // 프로덕션 환경에서는 UUID, 테스트 환경에서는 짧은 ID 사용
        String id = System.getProperty("spring.profiles.active", "").contains("test") 
                ? generateShortId() 
                : UUID.randomUUID().toString();
                
        return LectureEntity.builder()
                .id(id)
                .user(user)
                .lectureType(lectureType)
                .lectureName(lectureName)
                .lectureDescription(lectureDescription)
                .lecturePrice(lecturePrice)
                .build();
    }

    public void update(LectureType lectureType, String lectureName, String lectureDescription, Integer lecturePrice) {
        this.lectureType = lectureType;
        this.lectureName = lectureName;
        this.lectureDescription = lectureDescription;
        this.lecturePrice = lecturePrice;
    }
}