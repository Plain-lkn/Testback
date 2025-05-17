package org.example.plain.domain.lecture.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.plain.common.ResponseField;
import org.example.plain.common.enums.Message;
import org.example.plain.domain.lecture.entity.LectureEntity;
import org.example.plain.domain.lecture.entity.LectureRevenueEntity;
import org.example.plain.domain.lecture.repository.LectureRepository;
import org.example.plain.domain.lecture.repository.LectureRevenueRepository;
import org.example.plain.domain.lecture.service.interfaces.LectureRevenueService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LectureRevenueServiceImpl implements LectureRevenueService {

    private final LectureRepository lectureRepository;
    private final LectureRevenueRepository lectureRevenueRepository;

    @Override
    @Transactional
    public ResponseField<Boolean> processLecturePayment(String paymentType, String orderId,
            String paymentKey, Integer amount) {
        // TODO: 외부 결제 시스템과 연동
        LectureRevenueEntity revenue = LectureRevenueEntity.builder()
                .orderId(orderId)
                .paymentKey(paymentKey)
                .price(amount)
                .build();

        lectureRevenueRepository.save(revenue);
        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, true);
    }

    @Override
    public ResponseField<Integer> getLectureProfit(String lectureId) {
        LectureEntity lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        Integer totalRevenue = lectureRevenueRepository.calculateTotalRevenueByLectureId(lectureId);
        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, totalRevenue);
    }

    @Override
    @Transactional
    public ResponseField<Void> handlePaymentFailure(String errorCode, String errorMessage, String orderId) {
        // TODO: 결제 실패 로그 기록
        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, null);
    }
} 