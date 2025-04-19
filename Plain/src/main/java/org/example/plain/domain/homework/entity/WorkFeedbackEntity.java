package org.example.plain.domain.homework.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.plain.domain.user.entity.User;

import java.time.LocalDateTime;

/**
 * 과제 피드백 엔티티
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "work_feedback", schema = "plain")
@IdClass(WorkFeedbackId.class)
public class WorkFeedbackEntity {

    @Id
    @Column(name = "h_id")
    private String workId;

    @Id
    @Column(name = "user_id")
    private String userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "h_id", referencedColumnName = "h_id", insertable = false, updatable = false)
    private WorkEntity work;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User user;
    
    // 교수 ID
    @Column(name = "instructor_id")
    private String instructorId;
    
    // 피드백 내용
    @Column(length = 2000)
    private String feedback;
    
    // 과제 점수
    private int score;
    
    // 피드백 등록일
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // 피드백 수정일
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User instructor;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 