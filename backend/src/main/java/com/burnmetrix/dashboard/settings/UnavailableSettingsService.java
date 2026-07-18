package com.burnmetrix.dashboard.settings;

import org.springframework.stereotype.Service;

@Service
public class UnavailableSettingsService implements SettingsService {

    @Override
    public SettingsResponse currentSettings() {
        return new SettingsResponse(
                new IntegrationStatus("Strava", false, "Not connected"),
                new IntegrationStatus("Google Calendar", false, "Not connected"),
                new IntegrationStatus("Weather", true, "Open-Meteo"),
                "kiosk",
                "imperial",
                "0.1.0-SNAPSHOT");
    }
}
