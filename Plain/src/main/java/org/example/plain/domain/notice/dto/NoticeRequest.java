package org.example.plain.domain.notice.dto;

import lombok.*;
import org.example.plain.domain.classLecture.entity.ClassLecture;
import org.example.plain.domain.notice.entity.NoticeEntity;
import org.example.plain.domain.user.entity.User;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeRequest{
    private String title;
    private String content;
//    private User user;
    private String c_id;

}
