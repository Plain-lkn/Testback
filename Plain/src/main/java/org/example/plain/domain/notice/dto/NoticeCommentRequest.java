package org.example.plain.domain.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.plain.domain.user.entity.User;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NoticeCommentRequest {

    private String commentId;
    private String noticeId;
    private String title;
    private String content;
    private User user;

}
