package org.example.plain.domain.homework.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.plain.domain.file.entity.FileEntity;
import org.example.plain.domain.file.entity.WorkFileEntity;
import org.example.plain.domain.user.entity.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "homework_member")
public class WorkMemberEntity {
    @EmbeddedId
    private WorkMemberId workMemberId;

    @MapsId("work")
    @ManyToOne
    @JoinColumn(name = "h_id")
    private WorkEntity work;

    @MapsId("memberUser")
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_submit")
    @Builder.Default
    private boolean isSubmited = false;

    @Column(name = "is_late")
    @Builder.Default
    private boolean isLate = false;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "h_id", referencedColumnName = "h_id")
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @Builder.Default
    private List<WorkFileEntity> fileEntities = new ArrayList<>();

    public static WorkMemberEntity makeWorkMemberEntity(User userId, WorkEntity workId) {
        WorkMemberEntity entity = new WorkMemberEntity();
        entity.setWorkMemberId(new WorkMemberId(workId.getWorkId(), userId.getId()));
        entity.setWork(workId);
        entity.setUser(userId);
        entity.setFileEntities(new ArrayList<>());
        return entity;
    }
}

