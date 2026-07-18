package com.burnmetrix.dashboard.weather;

import java.time.Instant;

import org.springframework.stereotype.Service;

@Service
public class MockWeatherService implements WeatherService {

    @Override
    public WeatherCurrentResponse currentConditions() {
        return new WeatherCurrentResponse("mock", "Clear", 72.0, 70.0, 32, 8.4, 5, Instant.now(), "5:42 AM", "8:31 PM");
    }
}
