package org.example.plain.domain.lecture.entity.member;

import jakarta.persistence.*;
import lombok.*;
import org.example.plain.domain.lecture.entity.LectureEntity;
import org.example.plain.domain.user.entity.User;

@Entity
@Table(name = "lecture_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@IdClass(LectureMemberId.class)
public class LectureMemberEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private LectureEntity lecture;

    public static LectureMemberEntity create(User user, LectureEntity lecture) {
        return LectureMemberEntity.builder()
                .user(user)
                .lecture(lecture)
                .build();
    }
} 