package com.burnmetrix.dashboard.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;

class IcalParserTests {

    private final IcalParser parser = new IcalParser(Clock.fixed(Instant.parse("2026-07-17T12:00:00Z"), ZoneId.of("UTC")));

    @Test
    void parsesUpcomingSingleEvents() {
        String feed = """
                BEGIN:VCALENDAR
                BEGIN:VEVENT
                UID:event-1
                SUMMARY:Dentist
                LOCATION:Downtown
                DTSTART:20260718T090000Z
                DTEND:20260718T100000Z
                END:VEVENT
                END:VCALENDAR
                """;

        List<CalendarEventResponse> events = parser.events(
                feed,
                Instant.parse("2026-07-17T00:00:00Z"),
                Instant.parse("2026-07-31T00:00:00Z"),
                ZoneId.of("UTC"));

        assertThat(events).hasSize(1);
        assertThat(events.get(0).title()).isEqualTo("Dentist");
        assertThat(events.get(0).location()).isEqualTo("Downtown");
        assertThat(events.get(0).startsAt()).isEqualTo(Instant.parse("2026-07-18T09:00:00Z"));
    }

    @Test
    void expandsWeeklyRecurringEvents() {
        String feed = """
                BEGIN:VCALENDAR
                BEGIN:VEVENT
                UID:event-2
                SUMMARY:Soccer
                DTSTART:20260713T180000Z
                DTEND:20260713T190000Z
                RRULE:FREQ=WEEKLY;COUNT=4;BYDAY=MO,WE
                END:VEVENT
                END:VCALENDAR
                """;

        List<CalendarEventResponse> events = parser.events(
                feed,
                Instant.parse("2026-07-17T00:00:00Z"),
                Instant.parse("2026-08-15T00:00:00Z"),
                ZoneId.of("UTC"));

        assertThat(events)
                .extracting(CalendarEventResponse::startsAt)
                .containsExactly(
                        Instant.parse("2026-07-20T18:00:00Z"),
                        Instant.parse("2026-07-22T18:00:00Z"));
    }
}
