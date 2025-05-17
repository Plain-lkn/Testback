package org.example.plain.domain.notice.dto;

import lombok.*;
import org.example.plain.domain.notice.entity.NoticeCommentEntity;
import org.example.plain.domain.user.entity.User;

import java.time.LocalDateTime;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeCommentResponse {

    private String noticeId;
    private String commentId;
    private String title;
    private String content;
    private LocalDateTime createDate;
    private LocalDateTime modifiedAt;
    private User user;

    public static NoticeCommentResponse from(NoticeCommentEntity noticeCommentEntity) {
        NoticeCommentResponse response = new NoticeCommentResponse();
        response.noticeId = noticeCommentEntity.getNoticeId();
        response.commentId = noticeCommentEntity.getNoticeId();
        response.title = noticeCommentEntity.getTitle();
        response.content = noticeCommentEntity.getContent();
        response.user = noticeCommentEntity.getUser();
        response.createDate = noticeCommentEntity.getCreateDate();
        response.modifiedAt = noticeCommentEntity.getModifiedAt();
        return response;
    }

}
