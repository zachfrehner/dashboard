package com.burnmetrix.dashboard.calendar;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class MockCalendarService implements CalendarService {

    @Override
    public List<CalendarEventResponse> upcomingEvents() {
        Instant now = Instant.now();
        return List.of(
                new CalendarEventResponse("mock-calendar-1", "Recovery spin", "Garage trainer",
                        now.plus(2, ChronoUnit.HOURS), now.plus(3, ChronoUnit.HOURS), "mock"),
                new CalendarEventResponse("mock-calendar-2", "Bike fit notes review", "Home",
                        now.plus(1, ChronoUnit.DAYS), now.plus(1, ChronoUnit.DAYS).plus(30, ChronoUnit.MINUTES), "mock"));
    }
}

