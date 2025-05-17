package org.example.plain.domain.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class LectureVideoFileData extends FileData {
    private String lectureId;
    private String curriculumId;
    private Integer playTime;
    private String videoType;
}
