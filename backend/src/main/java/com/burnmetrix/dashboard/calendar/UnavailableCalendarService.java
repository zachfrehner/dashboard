package com.burnmetrix.dashboard.calendar;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class UnavailableCalendarService implements CalendarService {

    @Override
    public List<CalendarEventResponse> upcomingEvents() {
        return List.of();
    }
}
