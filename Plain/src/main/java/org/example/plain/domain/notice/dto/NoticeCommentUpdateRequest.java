package org.example.plain.domain.notice.dto;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import org.example.plain.domain.user.entity.User;

@Getter
@ToString
@Data
public class NoticeCommentUpdateRequest {

    private String commentId;
    private String noticeId;
    private String title;
    private String content;
    private User user;

}
