package org.example.plain.domain.homework.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 과제 피드백 복합 기본키 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkFeedbackId implements Serializable {
    private String workId;
    private String userId;
} 