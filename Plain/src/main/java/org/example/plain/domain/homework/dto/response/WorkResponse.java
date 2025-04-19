package org.example.plain.domain.homework.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.plain.domain.file.dto.FileInfo;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class WorkResponse {
    private String workId;
    private String boardId;
    private String groupId;
    private String writer;
    private String title;
    private String content;
    private LocalDateTime deadline;
    private List<FileInfo> fileList;
} 