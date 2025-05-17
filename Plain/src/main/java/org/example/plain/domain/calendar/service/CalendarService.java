package org.example.plain.domain.calendar.service;

import lombok.RequiredArgsConstructor;
import org.example.plain.common.ResponseField;
import org.example.plain.common.enums.Category;
import org.example.plain.common.enums.Message;
import org.example.plain.domain.calendar.dto.CalendarRequest;
import org.example.plain.domain.calendar.dto.CalendarResponse;
import org.example.plain.domain.calendar.entity.CalendarEntity;
import org.example.plain.domain.calendar.repository.CalendarRepository;
import org.example.plain.domain.user.entity.User;
import org.example.plain.domain.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final UserRepository userRepository;
    private final CalendarRepository calendarRepository;

    @Transactional
    public ResponseField<CalendarResponse> insertCalendar(CalendarRequest calendarRequest, String userId) {
        // User ID로 User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // NoticeEntity 생성 (User 포함)
        CalendarEntity insertCalendar = CalendarEntity.create(
                calendarRequest.getTitle(),
                calendarRequest.getContent(),
                calendarRequest.getCategory(),
                user
        );

        // 저장
        CalendarEntity calendarEntity = calendarRepository.save(insertCalendar);

        // NoticeResponse 생성 및 반환
        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, CalendarResponse.from(calendarEntity));
    }

    @Transactional
    public ResponseField<CalendarResponse> updateCalendar(CalendarRequest calendarRequest,String userId) {

        CalendarEntity calendarEntity = calendarRepository.findById(calendarRequest.getCalId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!calendarEntity.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }
        
        calendarEntity.update(
                calendarRequest.getCalId(),
                calendarRequest.getTitle(),
                calendarRequest.getContent()

        );

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, CalendarResponse.from(calendarEntity));
    }

    public ResponseField<List<CalendarResponse>> getCalendar(Category category){
        List<CalendarEntity> calendars = calendarRepository.findByCategory(category);

        if (calendars.isEmpty()) {
            throw new NoSuchElementException("해당 카테고리에 대한 캘린더가 없습니다.");
        }
        
        // List<Calendar> -> List<CalendarResponse> 변환
        List<CalendarResponse> calendarResponses = calendars.stream()
                .map(CalendarResponse::from)
                .collect(Collectors.toList());

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, calendarResponses);
    }

    public ResponseField<CalendarResponse> getDetailCalendar(Long calId){
        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, CalendarResponse.from(calendarRepository.findById(calId).orElseThrow()));
    }

    @Transactional
    public void deleteCalendar(Long calId, String userId) {
        CalendarEntity calendarEntity = calendarRepository.findById(calId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!calendarEntity.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        calendarRepository.delete(calendarEntity);
    }

}
