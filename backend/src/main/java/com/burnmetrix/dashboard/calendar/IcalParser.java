package com.burnmetrix.dashboard.calendar;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class IcalParser {

    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private static final DateTimeFormatter DATE_TIME_MINUTES = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm");

    private final Clock clock;

    public IcalParser(Clock clock) {
        this.clock = clock;
    }

    public List<CalendarEventResponse> events(String content, Instant windowStart, Instant windowEnd, ZoneId defaultZone) {
        List<CalendarEventResponse> events = new ArrayList<>();
        for (List<String> block : eventBlocks(unfold(content))) {
            events.addAll(expand(block, windowStart, windowEnd, defaultZone));
        }
        return events.stream()
                .sorted(Comparator.comparing(CalendarEventResponse::startsAt))
                .toList();
    }

    private List<CalendarEventResponse> expand(List<String> block, Instant windowStart, Instant windowEnd, ZoneId defaultZone) {
        String uid = value(block, "UID", "ical-" + Math.abs(block.hashCode()));
        String title = clean(value(block, "SUMMARY", "Untitled"));
        String location = clean(value(block, "LOCATION", ""));
        DateValue starts = dateValue(line(block, "DTSTART"), defaultZone);
        if (starts == null) {
            return List.of();
        }
        DateValue ends = dateValue(line(block, "DTEND"), starts.zone());
        Duration duration = duration(starts, ends);
        Map<String, String> rule = rrule(value(block, "RRULE", ""));
        Set<Instant> excluded = exdates(block, starts.zone());

        if (rule.isEmpty()) {
            Instant end = starts.instant().plus(duration);
            return end.isBefore(windowStart) || starts.instant().isAfter(windowEnd) || excluded.contains(starts.instant())
                    ? List.of()
                    : List.of(response(uid, title, location, starts.instant(), end));
        }

        List<CalendarEventResponse> occurrences = new ArrayList<>();
        String frequency = rule.getOrDefault("FREQ", "").toUpperCase(Locale.ROOT);
        int interval = parseInt(rule.get("INTERVAL"), 1);
        int count = parseInt(rule.get("COUNT"), 1000);
        Instant until = until(rule.get("UNTIL"), starts.zone(), windowEnd);
        int generated = 0;
        int step = 0;
        while (generated < count && step < 1500) {
            List<ZonedDateTime> candidates = candidates(starts.dateTime(), frequency, interval, step, rule);
            for (ZonedDateTime candidate : candidates) {
                if (candidate.toInstant().isBefore(starts.instant())) {
                    continue;
                }
                generated++;
                Instant start = candidate.toInstant();
                Instant end = start.plus(duration);
                if (start.isAfter(until) || start.isAfter(windowEnd)) {
                    return occurrences;
                }
                if (!end.isBefore(windowStart) && !excluded.contains(start)) {
                    occurrences.add(response(uid + "-" + start.toEpochMilli(), title, location, start, end));
                }
                if (generated >= count) {
                    break;
                }
            }
            step++;
        }
        return occurrences;
    }

    private static CalendarEventResponse response(String id, String title, String location, Instant startsAt, Instant endsAt) {
        return new CalendarEventResponse(id, title, location, startsAt, endsAt, "ical");
    }

    private static List<ZonedDateTime> candidates(ZonedDateTime start, String frequency, int interval, int step, Map<String, String> rule) {
        return switch (frequency) {
            case "DAILY" -> List.of(start.plusDays((long) step * interval));
            case "WEEKLY" -> weeklyCandidates(start, interval, step, rule.get("BYDAY"));
            case "MONTHLY" -> List.of(start.plusMonths((long) step * interval));
            case "YEARLY" -> List.of(start.plusYears((long) step * interval));
            default -> step == 0 ? List.of(start) : List.of();
        };
    }

    private static List<ZonedDateTime> weeklyCandidates(ZonedDateTime start, int interval, int step, String byDay) {
        ZonedDateTime weekStart = start.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks((long) step * interval);
        if (byDay == null || byDay.isBlank()) {
            return List.of(start.plusWeeks((long) step * interval));
        }
        List<ZonedDateTime> candidates = new ArrayList<>();
        for (String token : byDay.split(",")) {
            DayOfWeek day = dayOfWeek(token.trim());
            if (day != null) {
                candidates.add(weekStart.with(TemporalAdjusters.nextOrSame(day)).with(LocalTime.from(start)));
            }
        }
        candidates.sort(Comparator.naturalOrder());
        return candidates;
    }

    private static DayOfWeek dayOfWeek(String value) {
        String day = value.replaceAll("^[+-]?\\d+", "").toUpperCase(Locale.ROOT);
        return switch (day) {
            case "MO" -> DayOfWeek.MONDAY;
            case "TU" -> DayOfWeek.TUESDAY;
            case "WE" -> DayOfWeek.WEDNESDAY;
            case "TH" -> DayOfWeek.THURSDAY;
            case "FR" -> DayOfWeek.FRIDAY;
            case "SA" -> DayOfWeek.SATURDAY;
            case "SU" -> DayOfWeek.SUNDAY;
            default -> null;
        };
    }

    private static Duration duration(DateValue starts, DateValue ends) {
        if (ends != null && ends.instant().isAfter(starts.instant())) {
            return Duration.between(starts.instant(), ends.instant());
        }
        return starts.allDay() ? Duration.ofDays(1) : Duration.ofHours(1);
    }

    private static Instant until(String value, ZoneId zone, Instant fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        DateValue dateValue = parseDateValue(value, zone, false);
        return dateValue == null ? fallback : dateValue.instant();
    }

    private static Set<Instant> exdates(List<String> block, ZoneId defaultZone) {
        Set<Instant> values = new HashSet<>();
        for (String line : block) {
            if (!name(line).equals("EXDATE")) {
                continue;
            }
            ZoneId zone = zone(line, defaultZone);
            for (String item : rawValue(line).split(",")) {
                DateValue date = parseDateValue(item, zone, line.toUpperCase(Locale.ROOT).contains("VALUE=DATE"));
                if (date != null) {
                    values.add(date.instant());
                }
            }
        }
        return values;
    }

    private static DateValue dateValue(String line, ZoneId defaultZone) {
        if (line == null) {
            return null;
        }
        return parseDateValue(rawValue(line), zone(line, defaultZone), line.toUpperCase(Locale.ROOT).contains("VALUE=DATE"));
    }

    private static DateValue parseDateValue(String value, ZoneId zone, boolean allDay) {
        try {
            String trimmed = value.trim();
            if (allDay || trimmed.length() == 8) {
                return new DateValue(LocalDate.parse(trimmed, DATE).atStartOfDay(zone), true);
            }
            if (trimmed.endsWith("Z")) {
                LocalDateTime dateTime = LocalDateTime.parse(trimmed.substring(0, trimmed.length() - 1), formatter(trimmed.length() - 1));
                return new DateValue(dateTime.atZone(ZoneId.of("UTC")).withZoneSameInstant(zone), false);
            }
            return new DateValue(LocalDateTime.parse(trimmed, formatter(trimmed.length())).atZone(zone), false);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static DateTimeFormatter formatter(int length) {
        return length == 13 ? DATE_TIME_MINUTES : DATE_TIME;
    }

    private static ZoneId zone(String line, ZoneId defaultZone) {
        String upper = line.toUpperCase(Locale.ROOT);
        int index = upper.indexOf("TZID=");
        if (index < 0) {
            return defaultZone;
        }
        int start = index + "TZID=".length();
        int end = line.indexOf(';', start);
        int colon = line.indexOf(':', start);
        if (end < 0 || (colon >= 0 && colon < end)) {
            end = colon;
        }
        try {
            return ZoneId.of(line.substring(start, end));
        } catch (Exception ignored) {
            return defaultZone;
        }
    }

    private static Map<String, String> rrule(String value) {
        Map<String, String> map = new HashMap<>();
        for (String pair : value.split(";")) {
            int index = pair.indexOf('=');
            if (index > 0) {
                map.put(pair.substring(0, index).toUpperCase(Locale.ROOT), pair.substring(index + 1));
            }
        }
        return map;
    }

    private static List<String> unfold(String content) {
        List<String> lines = new ArrayList<>();
        for (String raw : content.replace("\r\n", "\n").replace('\r', '\n').split("\n")) {
            if ((raw.startsWith(" ") || raw.startsWith("\t")) && !lines.isEmpty()) {
                int last = lines.size() - 1;
                lines.set(last, lines.get(last) + raw.substring(1));
            } else {
                lines.add(raw);
            }
        }
        return lines;
    }

    private static List<List<String>> eventBlocks(List<String> lines) {
        List<List<String>> blocks = new ArrayList<>();
        List<String> current = null;
        for (String line : lines) {
            if ("BEGIN:VEVENT".equalsIgnoreCase(line)) {
                current = new ArrayList<>();
            } else if ("END:VEVENT".equalsIgnoreCase(line) && current != null) {
                blocks.add(current);
                current = null;
            } else if (current != null) {
                current.add(line);
            }
        }
        return blocks;
    }

    private static String line(List<String> block, String property) {
        return block.stream().filter(item -> name(item).equals(property)).findFirst().orElse(null);
    }

    private static String value(List<String> block, String property, String fallback) {
        String line = line(block, property);
        return line == null ? fallback : rawValue(line);
    }

    private static String rawValue(String line) {
        int index = line.indexOf(':');
        return index < 0 ? "" : line.substring(index + 1);
    }

    private static String name(String line) {
        int colon = line.indexOf(':');
        int semi = line.indexOf(';');
        int end = semi >= 0 && semi < colon ? semi : colon;
        return end < 0 ? line.toUpperCase(Locale.ROOT) : line.substring(0, end).toUpperCase(Locale.ROOT);
    }

    private static String clean(String value) {
        return value.replace("\\n", " ")
                .replace("\\N", " ")
                .replace("\\,", ",")
                .replace("\\;", ";")
                .replace("\\\\", "\\")
                .trim();
    }

    private static int parseInt(String value, int fallback) {
        try {
            return value == null ? fallback : Integer.parseInt(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private record DateValue(ZonedDateTime dateTime, boolean allDay) {
        Instant instant() {
            return dateTime.toInstant();
        }

        ZoneId zone() {
            return dateTime.getZone();
        }
    }
}
