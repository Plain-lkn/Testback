package org.example.plain.domain.lecture.service.interfaces;

import org.example.plain.common.ResponseField;

public interface LectureRevenueService {
    ResponseField<Boolean> processLecturePayment(String paymentType, String orderId, String paymentKey, Integer amount);
    ResponseField<Integer> getLectureProfit(String lectureId);
    ResponseField<Void> handlePaymentFailure(String errorCode, String errorMessage, String orderId);
} 