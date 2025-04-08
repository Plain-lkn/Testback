package org.example.plain.domain.file.dto;

import lombok.*;
import org.example.plain.domain.homework.entity.WorkEntity;
import org.example.plain.domain.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

@EqualsAndHashCode(callSuper = true)
@Data
public class SubmitFileData extends FileData {
    private WorkEntity workId;
    private User userId;

    @Builder
    public SubmitFileData(MultipartFile file, String filename, WorkEntity workId, User userId) {
        super(file,filename);
        this.workId = workId;
        this.userId = userId;
    }
}
