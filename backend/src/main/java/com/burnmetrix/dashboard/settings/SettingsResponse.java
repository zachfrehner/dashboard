package com.burnmetrix.dashboard.settings;

public record SettingsResponse(
        IntegrationStatus strava,
        IntegrationStatus googleCalendar,
        IntegrationStatus weather,
        String displayMode,
        String units,
        String version) {
}

