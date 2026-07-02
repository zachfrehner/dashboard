package com.burnmetrix.dashboard.calendar;

import java.time.Instant;

public record CalendarEventResponse(
        String id,
        String title,
        String location,
        Instant startsAt,
        Instant endsAt,
        String provider) {
}

