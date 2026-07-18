package com.burnmetrix.dashboard.settings;

import com.burnmetrix.dashboard.calendar.CalendarProperties;
import org.springframework.stereotype.Service;

@Service
public class DashboardSettingsService implements SettingsService {

    private final CalendarProperties calendarProperties;

    public DashboardSettingsService(CalendarProperties calendarProperties) {
        this.calendarProperties = calendarProperties;
    }

    @Override
    public SettingsResponse currentSettings() {
        return new SettingsResponse(
                new IntegrationStatus("Strava", false, "Not connected"),
                new IntegrationStatus("Calendar", calendarProperties.configured(), calendarProperties.configured() ? "iCalendar" : "Not connected"),
                new IntegrationStatus("Weather", true, "Open-Meteo"),
                "kiosk",
                "imperial",
                "0.1.0-SNAPSHOT");
    }
}
