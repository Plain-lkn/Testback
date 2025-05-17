package org.example.plain.domain.calendar.repository;

import org.example.plain.common.enums.Category;
import org.example.plain.domain.calendar.entity.CalendarEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalendarRepository extends JpaRepository<CalendarEntity, Long> {
    List<CalendarEntity> findByCategory(Category category);
}
