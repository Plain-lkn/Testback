package org.example.plain.domain.notice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.example.plain.domain.user.entity.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "notice_comment")
public class NoticeCommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "comment_id", unique = true, nullable = false)
    private String commentId;

    @Column(name = "notice_id", nullable = false)
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

    @Column(name = "create_date")
    @CreatedDate
    private LocalDateTime createDate;

    @Column(name = "modified_at")
    @LastModifiedDate
    private LocalDateTime modifiedAt;

    public static NoticeCommentEntity create(String title, String content, String noticeId, User user){
        NoticeCommentEntity noticeCommentEntity = new NoticeCommentEntity();
        noticeCommentEntity.noticeId = noticeId;
        noticeCommentEntity.title = title;
        noticeCommentEntity.content = content;
        noticeCommentEntity.user = user;
        noticeCommentEntity.userId = user.getId();
        noticeCommentEntity.createDate = LocalDateTime.now();
        noticeCommentEntity.modifiedAt = LocalDateTime.now();
        return noticeCommentEntity;
    }

    public void update(String commentId, String noticeId, String title, String content) {
        this.commentId = commentId;
        this.noticeId = noticeId;
        this.title = title;
        this.content = content;
        modifiedAt = LocalDateTime.now();
    }
}
