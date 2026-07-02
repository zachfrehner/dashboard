package com.burnmetrix.dashboard.calendar;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarEventRepository extends JpaRepository<CalendarEventEntity, Long> {
}

