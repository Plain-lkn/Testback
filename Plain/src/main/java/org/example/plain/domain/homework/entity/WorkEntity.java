package org.example.plain.domain.homework.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.plain.domain.board.entity.BoardEntity;
import org.example.plain.domain.homework.dto.Work;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;


@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@DiscriminatorValue("Work")
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "homework")
public class WorkEntity extends BoardEntity {

    @Column(name = "h_id",unique = true,nullable = false)
    private String workId;

    @Column(name = "submit_date", nullable = false)
    private LocalDateTime deadline;

    public WorkEntity(String classId, String title, String content, String type,
                     String userId, String workId, LocalDateTime deadline) {
        super(userId, classId, title, content, type);
        this.workId = workId;
        this.deadline = deadline;
    }
    
    public WorkEntity(String boardId,String classId, String title, String content, String type,
                      String userId, String workId, LocalDateTime deadline) {
        super(boardId, userId, classId, title, content, type);
        this.workId = workId;
        this.deadline = deadline;
    }

    public static WorkEntity workToWorkEntity(Work work) {
        return WorkEntity.builder()
                .boardId(work.getBoardId())
                .classId(work.getGroupId())
                .title(work.getTitle())
                .content(work.getContent())
                .type(work.getType())
                .userId(work.getUserId())
                .workId(work.getWorkId())
                .deadline(work.getDeadline())
                .build();
    }

    public static WorkEntity chaingeWorkEntitytoWork(Work work, WorkEntity workEntity) {
        return WorkEntity.builder()
                .boardId(workEntity.getBoardId())
                .classId(workEntity.getClassId())
                .title(work.getTitle())
                .content(work.getContent())
                .type(work.getType())
                .userId(workEntity.getUserId())
                .workId(workEntity.getWorkId())
                .deadline(work.getDeadline())
                .build();
    }

    public void setWorkId(String workId) {
        if (workId != null) {
            this.workId = workId;
        }
    }

    public void setDeadline(LocalDateTime deadline) {
        if (deadline != null) {
            this.deadline = deadline;
        }
    }
}
