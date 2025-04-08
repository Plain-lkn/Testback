package org.example.plain.domain.classLecture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.plain.domain.classLecture.dto.ClassAddRequest;
import org.example.plain.domain.classMember.entity.ClassMember;
import org.example.plain.domain.classMember.repository.ClassMemberRepository;
import org.example.plain.domain.user.entity.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "groupclass")
public class ClassLecture {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "g_id")
    private String id;

    @OneToMany(mappedBy = "classLecture", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClassMember> members = new ArrayList<>();

    @Column(name = "g_name")
    private String title;

    private String description;

    @Column(name = "g_code")
    private String code;

    @Column(name = "image_url")
    private String classImg;

    @Column(name = "maxmember")
    private Long maxMember;

    @ManyToOne
    @JoinColumn(name = "creator", nullable = false)
    private User instructor;

    public void addMember(User user, ClassMemberRepository classMemberRepository) {
        ClassMember classMember = new ClassMember(this, user);
        this.members.add(classMember);
        classMemberRepository.save(classMember);
    }

    public void updateClass(ClassAddRequest classAddRequest) {
        this.title = classAddRequest.title();
        this.description = classAddRequest.description();
        this.classImg = classAddRequest.classImg();
        this.maxMember = classAddRequest.maxMember();
    }
}
