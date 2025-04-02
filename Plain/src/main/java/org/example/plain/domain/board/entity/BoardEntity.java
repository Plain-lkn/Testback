package org.example.plain.domain.board.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.plain.domain.classLecture.entity.ClassLecture;
import org.example.plain.domain.user.entity.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.relational.core.sql.In;

import java.time.LocalDateTime;


@SuperBuilder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "board_type", discriminatorType = DiscriminatorType.STRING)
@Table(name = "board")
public class BoardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "b_id", unique = true, nullable = false)
    private String boardId;

    @Column(name = "user_id", insertable = false, updatable = false)
    private String userId;

    @Column(name = "c_id", insertable = false, updatable = false)
    private String classId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "c_id", referencedColumnName = "c_id")
    private ClassLecture group;

    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(name = "title")
    private String title;

    @Column(name = "context")
    private String content;

    @Column(name = "board_type", insertable = false, updatable = false)
    private String type;

    @Column(name = "create_date")
    @CreatedDate
    private LocalDateTime createDate;

    public BoardEntity (String userId, String classId, String title, String content, String type) {
        this.userId = userId;
        this.classId = classId;
        this.title = title;
        this.content = content;
        this.type = type;
        this.createDate = LocalDateTime.now();
    }

    public BoardEntity(String boardId, String userId, String classId, String title, String content, String type) {
        this.boardId = boardId;
        this.userId = userId;
        this.classId = classId;
        this.title = title;
        this.content = content;
        this.type = type;
        this.createDate = LocalDateTime.now();
    }


    public void setBoardId(String boardId) {
        if (boardId != null) {
            this.boardId = boardId;
        }
    }

    public void setGroup(ClassLecture group) {
        if (group != null) {
            this.group = group;
        }
    }

    public void setClassId(String groupId) {
        if (group != null) {
            this.classId = groupId;
        }
    }

    public void setUser(User user) {
        if (user != null) {
            this.user = user;
        }
    }

    public void setUserId(String userId) {
        if (userId != null) {
            this.userId = userId;
        }
    }

    public void setTitle(String title) {
        if (title != null) {
            this.title = title;
        }
    }

    public void setContent(String content) {
        if (content != null) {
            this.content = content;
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCreateDate(LocalDateTime createDate) {
        if (createDate != null) {
            this.createDate = createDate;
        }
    }
}
