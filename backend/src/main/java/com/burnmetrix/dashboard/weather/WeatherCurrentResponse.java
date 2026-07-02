package com.burnmetrix.dashboard.weather;

import java.time.Instant;

public record WeatherCurrentResponse(
        String provider,
        String condition,
        double temperatureF,
        double feelsLikeF,
        int humidityPercent,
        double windMph,
        int uvIndex,
        Instant observedAt) {
}

