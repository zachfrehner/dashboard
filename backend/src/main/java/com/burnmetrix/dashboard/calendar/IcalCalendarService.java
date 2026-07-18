package com.burnmetrix.dashboard.calendar;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class IcalCalendarService implements CalendarService {

    private final CalendarProperties properties;
    private final IcalParser parser;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public IcalCalendarService(CalendarProperties properties) {
        this.properties = properties;
        this.parser = new IcalParser(Clock.systemDefaultZone());
    }

    @Override
    public List<CalendarEventResponse> upcomingEvents() {
        if (!properties.configured()) {
            return List.of();
        }

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url()))
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400 || response.body().isBlank()) {
                return List.of();
            }
            Instant now = Instant.now();
            Instant horizon = now.plus(Duration.ofDays(properties.safeLookAheadDays()));
            return parser.events(response.body(), now, horizon, ZoneId.systemDefault()).stream()
                    .limit(properties.safeMaxEvents())
                    .toList();
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private String url() {
        String value = properties.icalUrl().trim();
        if (value.regionMatches(true, 0, "webcal://", 0, "webcal://".length())) {
            return "https://" + value.substring("webcal://".length());
        }
        return value;
    }
}
