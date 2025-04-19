package org.example.plain.domain.homework.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.plain.domain.board.dto.Board;
import org.example.plain.domain.homework.entity.WorkEntity;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class Work extends Board {
    private String workId;
    private LocalDateTime deadline;
    private List<MultipartFile> fileList;

    public Work(){
    }

    @Builder
    public Work(String boardId, String groupId, String writer, String workId, String title, String content, LocalDateTime deadline, List<MultipartFile> fileList) {
        super(boardId,groupId,writer,"WORK",title,content,deadline);
        this.workId = workId;
        this.deadline = deadline;
        this.fileList = fileList;
    }

    public static Work changeWorkEntity(WorkEntity workEntity){
        return Work.builder()
                .workId(workEntity.getWorkId())
                .deadline(workEntity.getDeadline())
                .groupId(workEntity.getClassId())
                .boardId(workEntity.getBoardId())
                .writer(workEntity.getClassId())
                .title(workEntity.getTitle())
                .content(workEntity.getContent())
                .build();
    }

}
