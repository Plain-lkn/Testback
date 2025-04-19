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
public class FileData {
    private MultipartFile file;
    private String filename;
//    private String referenceType; // 예: "BOARD", "PROFILE", "MESSAGE"
//    private String referenceId;   // 참조하는 객체의 ID
}
