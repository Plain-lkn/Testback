package org.example.plain.domain.homework.interfaces;

import org.example.plain.domain.homework.dto.Work;
import org.example.plain.domain.homework.dto.WorkSubmitListResponse;

import java.util.List;

public interface WorkService {
    void insertWork(Work work, String groupId, String userId);

    void updateWork(Work work, String workId, String userId);

    Work selectWork(String workId);

    void deleteWork(String workId);

    List<Work> selectGroupWorks(String groupId);
}
