package com.burnmetrix.dashboard.weather;

import java.time.Instant;

public record WeatherCurrentResponse(
        String provider,
        String condition,
        Double temperatureF,
        Double feelsLikeF,
        Integer humidityPercent,
        Double windMph,
        Integer uvIndex,
        Instant observedAt,
        String sunrise,
        String sunset) {
}
