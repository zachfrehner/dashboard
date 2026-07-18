package com.burnmetrix.dashboard.weather;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Primary
public class OpenMeteoWeatherService implements WeatherService {

    private static final String API_BASE = "https://api.open-meteo.com/v1/forecast";

    private final WeatherProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public OpenMeteoWeatherService(WeatherProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public WeatherCurrentResponse currentConditions() {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url()))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                return unavailable();
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode current = root.path("current");
            JsonNode daily = root.path("daily");
            return new WeatherCurrentResponse(
                    "Open-Meteo",
                    condition(current.path("weather_code").asInt(-1)),
                    current.path("temperature_2m").asDouble(),
                    current.path("apparent_temperature").asDouble(current.path("temperature_2m").asDouble()),
                    current.path("relative_humidity_2m").asInt(0),
                    current.path("wind_speed_10m").asDouble(),
                    (int) Math.round(current.path("uv_index").asDouble(0)),
                    observedAt(current.path("time").asText("")),
                    displayTime(daily.path("sunrise").path(0).asText("")),
                    displayTime(daily.path("sunset").path(0).asText("")));
        } catch (Exception ignored) {
            return unavailable();
        }
    }

    private static WeatherCurrentResponse unavailable() {
        return new WeatherCurrentResponse("Open-Meteo", null, null, null, null, null, null, null, null, null);
    }

    private String url() {
        return API_BASE
                + "?latitude=" + properties.latitude()
                + "&longitude=" + properties.longitude()
                + "&current=" + encode("temperature_2m,apparent_temperature,relative_humidity_2m,wind_speed_10m,uv_index,weather_code")
                + "&daily=" + encode("sunrise,sunset")
                + "&temperature_unit=fahrenheit"
                + "&wind_speed_unit=mph"
                + "&timezone=auto";
    }

    private static Instant observedAt(String value) {
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(java.time.ZoneId.systemDefault()).toInstant();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String displayTime(String value) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return dateTime.format(DateTimeFormatter.ofPattern("h:mm a"));
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String condition(int code) {
        return switch (code) {
            case 0 -> "Clear";
            case 1, 2 -> "Partly cloudy";
            case 3 -> "Overcast";
            case 45, 48 -> "Fog";
            case 51, 53, 55, 56, 57 -> "Drizzle";
            case 61, 63, 65, 66, 67 -> "Rain";
            case 71, 73, 75, 77 -> "Snow";
            case 80, 81, 82 -> "Rain showers";
            case 85, 86 -> "Snow showers";
            case 95, 96, 99 -> "Thunderstorms";
            default -> "Weather";
        };
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
