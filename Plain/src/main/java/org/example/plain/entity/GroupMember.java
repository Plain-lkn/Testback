package org.example.plain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.plain.dto.GroupMemberDTO;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "\"group_member\"")
public class GroupMember implements Serializable {
    @EmbeddedId
    private GroupMemberId id;

    @MapsId("groupId")
    @ManyToOne
    @JoinColumn(name = "g_id")
    private Group group;

    @MapsId("userId")
    @ManyToOne
    @JoinColumn(name = "u_id")
    private User user;

    public GroupMember(Group group, User user) {
        this.id = new GroupMemberId(group.getGroupId(), user.getUserId());
        this.group = group;
        this.user = user;
    }

    public GroupMemberDTO toDTO() {
        return GroupMemberDTO.builder()
                .group(group)
                .user(user)
                .build();
    }
}
