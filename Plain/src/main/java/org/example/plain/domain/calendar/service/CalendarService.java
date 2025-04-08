package org.example.plain.domain.calendar.service;

import lombok.RequiredArgsConstructor;
import org.example.plain.common.ResponseField;
import org.example.plain.common.config.SecurityUtils;
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
    public ResponseField<CalendarResponse> insertCalendar(CalendarRequest calendarRequest) {
        // 현재 로그인한 사용자 정보 가져오기
        String userId = SecurityUtils.getUserId();
        
        // User ID로 User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // CalendarEntity 생성 (User 포함)
        CalendarEntity insertCalendar = CalendarEntity.create(
                calendarRequest.getTitle(),
                calendarRequest.getContent(),
                calendarRequest.getCategory(),
                user
        );

        // 저장
        CalendarEntity calendarEntity = calendarRepository.save(insertCalendar);

        // CalendarResponse 생성 및 반환
        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, CalendarResponse.from(calendarEntity));
    }

    @Transactional
    public ResponseField<CalendarResponse> updateCalendar(CalendarRequest calendarRequest) {
        // 현재 로그인한 사용자 정보 가져오기
        String userId = SecurityUtils.getUserId();
        
        // 캘린더 엔티티 조회
        CalendarEntity calendarEntity = calendarRepository.findById(calendarRequest.getCalId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 캘린더입니다."));
        
        // 권한 확인 - 자신의 캘린더만 수정 가능
        if (!calendarEntity.getUser().getId().equals(userId)) {
            return new ResponseField<>(Message.UNAUTHORIZES.name(), HttpStatus.UNAUTHORIZED, null);
        }

        calendarEntity.update(
                calendarRequest.getCalId(),
                calendarRequest.getTitle(),
                calendarRequest.getContent()
        );

        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, CalendarResponse.from(calendarEntity));
    }

    public ResponseField<List<CalendarResponse>> getCalendar(Category category){
        // 현재 로그인한 사용자 정보 가져오기
        String userId = SecurityUtils.getUserId();
        
        // 사용자의 캘린더만 조회
        List<CalendarEntity> calendars = calendarRepository.findByCategoryAndUserId(category, userId);

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
        // 현재 로그인한 사용자 정보 가져오기
        String userId = SecurityUtils.getUserId();
        
        // 캘린더 엔티티 조회
        CalendarEntity calendarEntity = calendarRepository.findById(calId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 캘린더입니다."));
        
        // 권한 확인 - 자신의 캘린더만 조회 가능
        if (!calendarEntity.getUser().getId().equals(userId)) {
            return new ResponseField<>(Message.UNAUTHORIZES.name(), HttpStatus.UNAUTHORIZED, null);
        }
        
        return new ResponseField<>(Message.OK.name(), HttpStatus.OK, CalendarResponse.from(calendarEntity));
    }

    @Transactional
    public void deleteCalendar(Long calId) {
        // 현재 로그인한 사용자 정보 가져오기
        String userId = SecurityUtils.getUserId();
        
        // 캘린더 엔티티 조회
        CalendarEntity calendarEntity = calendarRepository.findById(calId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 캘린더입니다."));
        
        // 권한 확인 - 자신의 캘린더만 삭제 가능
        if (!calendarEntity.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        
        calendarRepository.delete(calendarEntity);
    }
}
