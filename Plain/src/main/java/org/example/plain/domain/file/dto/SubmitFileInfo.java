package org.example.plain.domain.file.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.plain.domain.homework.entity.WorkEntity;
import org.example.plain.domain.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

@EqualsAndHashCode(callSuper = true)
@Data
public class SubmitFileInfo extends FileInfo{
    private WorkEntity workId;
    private User userId;

    @Builder
    public SubmitFileInfo(String filename,String filePath, WorkEntity workId, User userId) {
        super(filename, filePath);
        this.workId = workId;
        this.userId = userId;
    }
}
