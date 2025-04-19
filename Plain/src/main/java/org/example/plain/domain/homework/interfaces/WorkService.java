package org.example.plain.domain.homework.interfaces;

import org.example.plain.domain.homework.dto.Work;
import org.example.plain.domain.homework.dto.WorkSubmitListResponse;
import org.example.plain.domain.homework.dto.response.WorkResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface WorkService {
    void insertWork(Work work, String groupId, String userId);

    void updateWork(Work work, String workId, String userId);

    WorkResponse selectWork(String workId);

    void deleteWork(String workId);

    @Transactional(readOnly = true)
    Work selectWorkDto(String workId);

    List<Work> selectGroupWorks(String groupId);
}
