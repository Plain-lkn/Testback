package org.example.plain.domain.homework.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.plain.domain.file.dto.SubmitFileInfo;
import org.example.plain.domain.homework.entity.WorkMemberEntity;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkSubmitListResponse {
    private String workId;
    private String userId;
    private boolean isSubmit;
    private boolean isLate;

    public static WorkSubmitListResponse changeEntity(WorkMemberEntity workMemberEntity) {
        WorkSubmitListResponse workSubmitField = new WorkSubmitListResponse();
        workSubmitField.setWorkId(workMemberEntity.getWork().getWorkId());
        workSubmitField.setUserId(workMemberEntity.getUser().getId());
        workSubmitField.setSubmit(workMemberEntity.isSubmited());
        workSubmitField.setLate(workMemberEntity.isLate());

        return workSubmitField;
    }
}
