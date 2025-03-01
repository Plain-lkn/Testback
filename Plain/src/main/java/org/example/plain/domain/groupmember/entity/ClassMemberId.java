package org.example.plain.domain.groupmember.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class ClassMemberId implements Serializable {

    @Column(name = "class_id")
    private String classId;

    @Column(name = "user_id")
    private String userId;
}
