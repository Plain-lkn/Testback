package org.example.plain.domain.notice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.example.plain.domain.classLecture.entity.ClassLecture;
import org.example.plain.domain.user.entity.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "notice")
public class NoticeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notice_id", unique = true, nullable = false)
    private String noticeId;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "user_id")
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "g_id", nullable = false)
    private ClassLecture classLecture;


    @Column(name = "create_date")
    @CreatedDate
    private LocalDateTime createDate;

    @Column(name = "modified_at")
    @LastModifiedDate
    private LocalDateTime modifiedAt;

    private static final AtomicLong counter = new AtomicLong();

    public static NoticeEntity create(String title, String content, User user, ClassLecture classLecture) {
        NoticeEntity noticeEntity = new NoticeEntity();
        noticeEntity.title = title;
        noticeEntity.content = content;
        noticeEntity.user = user;
        noticeEntity.userId = user.getId();
        noticeEntity.classLecture = classLecture;
        noticeEntity.createDate = LocalDateTime.now();
        noticeEntity.modifiedAt = LocalDateTime.now();
        return noticeEntity;
    }

    public void update(String noticeId, String title, String content) {
        this.noticeId = noticeId;
        this.title = title;
        this.content = content;
        modifiedAt = LocalDateTime.now();
    }

}
