 package org.example.plain.domain.homework.interfaces;

import org.example.plain.domain.homework.dto.WorkSubmitField;
import org.example.plain.domain.homework.dto.WorkSubmitListResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface SubmissionService {
    /**
     * 과제 제출
     */
    void submit(WorkSubmitField workSubmitField);

    /**
     * 과제 제출물 조회
     */
    List<String> getSubmissionFiles(String workId, String userId);

    /**
     * 과제 제출 목록 조회
     */
    List<WorkSubmitListResponse> getSubmissionList(String workId);

    /**
     * 과제 제출 상태 확인
     */
    boolean isSubmitted(String workId, String userId);

    /**
     * 과제 제출 취소
     */
    void cancelSubmission(String workId, String userId);
}