package com.burnmetrix.dashboard.weather;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "burnmetrix.weather")
public record WeatherProperties(
        double latitude,
        double longitude,
        String locationName) {
}
