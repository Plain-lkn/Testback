package org.example.plain.domain.homework.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.plain.domain.file.dto.SubmitFileInfo;
import org.example.plain.domain.homework.entity.WorkMemberEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkSubmitFieldResponse {
    String workId;
    String userId;
    List<SubmitFileInfo> file;

    public static WorkSubmitFieldResponse changeEntity(WorkMemberEntity workMemberEntity) {
        WorkSubmitFieldResponse workSubmitField = new WorkSubmitFieldResponse();
        workSubmitField.setWorkId(workMemberEntity.getWork().getWorkId());
        workSubmitField.setUserId(workMemberEntity.getUser().getId());

        List<SubmitFileInfo> list = new ArrayList<>();

        workMemberEntity.getFileEntities().forEach(fileEntity -> {
            list.add(new SubmitFileInfo(fileEntity.getFilename(), fileEntity.getFilePath(), fileEntity.getWorkMember().getWork(), fileEntity.getUser()));
        });

        workSubmitField.setFile(list);
        return workSubmitField;
    }
}


