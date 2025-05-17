package org.example.plain.domain.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.plain.domain.user.entity.User;

@Getter
@ToString
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeUpdateRequest {

    private String noticeId;
    private String title;
    private String content;
    private User user;

}
