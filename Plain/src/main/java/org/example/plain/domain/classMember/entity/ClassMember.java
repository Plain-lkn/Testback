package org.example.plain.domain.classMember.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.plain.domain.classLecture.entity.ClassLecture;
import org.example.plain.domain.user.entity.User;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "class_member")
public class ClassMember {

    @EmbeddedId
    private ClassMemberId id;

    @ManyToOne
    @MapsId("classId")
    @JoinColumn(name = "g_id", nullable = false)
    private ClassLecture classLecture;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public ClassMember(ClassLecture classLecture, User user) {
        this.id = new ClassMemberId(classLecture.getId(), user.getId());
        this.classLecture = classLecture;
        this.user = user;
    }
}

