package com.burnmetrix.dashboard.settings;

import org.springframework.stereotype.Service;

@Service
public class MockSettingsService implements SettingsService {

    @Override
    public SettingsResponse currentSettings() {
        return new SettingsResponse(
                new IntegrationStatus("Strava", false, "mock"),
                new IntegrationStatus("Google Calendar", false, "mock"),
                new IntegrationStatus("Weather API", false, "mock"),
                "kiosk",
                "imperial",
                "0.1.0-SNAPSHOT");
    }
}

