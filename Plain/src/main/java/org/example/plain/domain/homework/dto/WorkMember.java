package org.example.plain.domain.homework.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.plain.domain.homework.entity.WorkMemberEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkMember {
    //private User user;
    private Work work;
    private boolean isSubmit;
    private boolean isLate;

    public static WorkMember changeEntity(WorkMemberEntity entity) {
        WorkMember workMember = new WorkMember();
        workMember.setWork(Work.changeWorkEntity(entity.getWork()));
        //workMember.setUser(entity.getUser());
        return workMember;
    }
}
