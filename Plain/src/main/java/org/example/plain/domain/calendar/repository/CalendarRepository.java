package org.example.plain.domain.calendar.repository;

import org.example.plain.common.enums.Category;
import org.example.plain.domain.calendar.entity.CalendarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CalendarRepository extends JpaRepository<CalendarEntity, Long> {
    List<CalendarEntity> findByCategory(Category category);
    
    @Query("SELECT c FROM CalendarEntity c WHERE c.category = :category AND c.user.id = :userId")
    List<CalendarEntity> findByCategoryAndUserId(@Param("category") Category category, @Param("userId") String userId);
}
