package org.example.plain.domain.groupmember.dto;

import lombok.Builder;
import lombok.Data;
import org.example.plain.domain.classLecture.entity.ClassLecture;
import org.example.plain.domain.groupmember.entity.GroupMember;
import org.example.plain.domain.groupmember.entity.GroupMemberId;
import org.example.plain.domain.user.dto.UserRequest;
import org.example.plain.domain.user.entity.User;

@Data
@Builder
public class GroupMemberDTO {
    private ClassLecture group;
    private UserRequest userRequest;

    public GroupMember toEntity() {
        GroupMemberId id = new GroupMemberId(group.getId(), userRequest.getId());
        return GroupMember.builder()
                .id(id)
                .group(group)
                .user(new User(userRequest))
                .build();
    }
}
