package com.burnmetrix.dashboard.calendar;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "burnmetrix.calendar")
public record CalendarProperties(
        String icalUrl,
        int lookAheadDays,
        int maxEvents) {

    public boolean configured() {
        return icalUrl != null && !icalUrl.isBlank();
    }

    public int safeLookAheadDays() {
        return lookAheadDays > 0 ? lookAheadDays : 180;
    }

    public int safeMaxEvents() {
        return maxEvents > 0 ? maxEvents : 10;
    }
}
