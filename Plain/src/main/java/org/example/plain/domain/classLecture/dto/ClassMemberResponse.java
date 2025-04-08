package org.example.plain.domain.classLecture.dto;

import org.example.plain.domain.classMember.entity.ClassMember;
import org.example.plain.domain.user.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public record ClassMemberResponse(
        String id,
        String username
) {
    public static ClassMemberResponse from(ClassMember classMember) {
        return new ClassMemberResponse(classMember.getId().getClassId(), classMember.getUser().getUsername());
    }

    public static List<ClassMemberResponse> from(List<ClassMember> classMembers) {
        return classMembers.stream()
                .map(ClassMemberResponse::from)
                .collect(Collectors.toList());
    }
}
