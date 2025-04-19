package org.example.plain.domain.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
public class WorkFileData extends FileData {
    private String workId;
    
    // 빈 생성자도 추가
    public WorkFileData() {
        super();
    }
} 