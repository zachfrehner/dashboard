package com.burnmetrix.dashboard.cycling;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.burnmetrix.dashboard.metabolic.MetabolicProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
@Primary
public class StravaCyclingService implements CyclingService {

    private static final String API_BASE = "https://www.strava.com/api/v3";
    private static final double METERS_TO_MILES = 0.000621371;
    private static final double METERS_TO_FEET = 3.28084;
    private static final int FTP_WATTS = 255;

    private final MetabolicProperties properties;
    private final ObjectMapper objectMapper;
    private final MockCyclingService fallback;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public StravaCyclingService(MetabolicProperties properties, ObjectMapper objectMapper, MockCyclingService fallback) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.fallback = fallback;
    }

    @Override
    public CyclingSummaryResponse summary(CyclingPeriod period) {
        try {
            if (!configured() || readToken() == null) {
                return fallback.summary(period);
            }
            List<JsonNode> activities = activities(period, 100);
            if (activities.isEmpty()) {
                return emptySummary(period);
            }

            int rideTimeSeconds = 0;
            int movingTimeSeconds = 0;
            double distanceMeters = 0;
            double elevationMeters = 0;
            double weightedSpeed = 0;
            double weightedPower = 0;
            double weightedNormalizedPower = 0;
            double weightedHeartRate = 0;
            double weightedCadence = 0;
            int powerWeight = 0;
            int normalizedPowerWeight = 0;
            int heartRateWeight = 0;
            int cadenceWeight = 0;
            double calories = 0;
            List<RideSummaryResponse> recentRides = new ArrayList<>();

            for (JsonNode activity : activities) {
                int moving = activity.path("moving_time").asInt(0);
                int elapsed = activity.path("elapsed_time").asInt(moving);
                rideTimeSeconds += elapsed;
                movingTimeSeconds += moving;
                distanceMeters += activity.path("distance").asDouble(0);
                elevationMeters += activity.path("total_elevation_gain").asDouble(0);
                weightedSpeed += activity.path("average_speed").asDouble(0) * moving;
                calories += calories(activity);

                if (activity.hasNonNull("average_watts")) {
                    weightedPower += activity.path("average_watts").asDouble() * moving;
                    powerWeight += moving;
                }
                if (activity.hasNonNull("weighted_average_watts")) {
                    weightedNormalizedPower += activity.path("weighted_average_watts").asDouble() * moving;
                    normalizedPowerWeight += moving;
                }
                if (activity.hasNonNull("average_heartrate")) {
                    weightedHeartRate += activity.path("average_heartrate").asDouble() * moving;
                    heartRateWeight += moving;
                }
                if (activity.hasNonNull("average_cadence")) {
                    weightedCadence += activity.path("average_cadence").asDouble() * moving;
                    cadenceWeight += moving;
                }
                if (recentRides.size() < 5) {
                    recentRides.add(summaryFromActivity(activity));
                }
            }

            double avgPower = weighted(powerWeight, weightedPower);
            double normalizedPower = weighted(normalizedPowerWeight, weightedNormalizedPower);
            double hours = movingTimeSeconds / 3600.0;
            double intensity = normalizedPower > 0 ? normalizedPower / FTP_WATTS : 0;
            double tss = hours > 0 && intensity > 0 ? hours * intensity * intensity * 100 : 0;

            int fatCalories = (int) Math.round(calories * 0.38);
            int carbCalories = (int) Math.round(Math.max(0, calories - fatCalories));
            return new CyclingSummaryResponse(
                    period,
                    round(distanceMeters * METERS_TO_MILES),
                    rideTimeSeconds,
                    movingTimeSeconds,
                    round(elevationMeters * METERS_TO_FEET),
                    (int) Math.round(calories),
                    round(weighted(movingTimeSeconds, weightedSpeed) * 2.23694),
                    (int) Math.round(avgPower),
                    (int) Math.round(normalizedPower),
                    FTP_WATTS,
                    (int) Math.round(weighted(heartRateWeight, weightedHeartRate)),
                    (int) Math.round(weighted(cadenceWeight, weightedCadence)),
                    round(tss),
                    round(tss * 0.78),
                    fatCalories,
                    carbCalories,
                    powerZones(avgPower),
                    heartRateZones(weighted(heartRateWeight, weightedHeartRate)),
                    recentRides);
        } catch (Exception ignored) {
            return fallback.summary(period);
        }
    }

    @Override
    public RideDetailResponse rideDetail(String rideId) {
        try {
            if (!configured() || readToken() == null) {
                return fallback.rideDetail(rideId);
            }
            JsonNode activity = stravaApi("/activities/" + rideId);
            JsonNode streams = stravaApi("/activities/" + rideId + "/streams?keys=time,heartrate,watts,altitude&key_by_type=true");
            CyclingSummaryResponse summary = summaryForActivity(activity);
            return new RideDetailResponse(
                    activity.path("id").asText(rideId),
                    activity.path("name").asText("Untitled ride"),
                    instant(activity),
                    summary,
                    chart(streams, "time", "watts"),
                    chart(streams, "time", "heartrate"),
                    altitudeChart(streams),
                    "Strava ride summary with BurnMetrix metabolic analysis available on the Calories page.",
                    activity.path("description").asText(""));
        } catch (Exception ignored) {
            return fallback.rideDetail(rideId);
        }
    }

    private CyclingSummaryResponse summaryForActivity(JsonNode activity) {
        int moving = activity.path("moving_time").asInt(0);
        int elapsed = activity.path("elapsed_time").asInt(moving);
        double avgPower = activity.path("average_watts").asDouble(0);
        double normalizedPower = activity.path("weighted_average_watts").asDouble(avgPower);
        double calories = calories(activity);
        double hours = moving / 3600.0;
        double intensity = normalizedPower > 0 ? normalizedPower / FTP_WATTS : 0;
        double tss = hours > 0 && intensity > 0 ? hours * intensity * intensity * 100 : 0;
        int fatCalories = (int) Math.round(calories * 0.38);
        int carbCalories = (int) Math.round(Math.max(0, calories - fatCalories));
        return new CyclingSummaryResponse(
                CyclingPeriod.TODAY,
                round(activity.path("distance").asDouble(0) * METERS_TO_MILES),
                elapsed,
                moving,
                round(activity.path("total_elevation_gain").asDouble(0) * METERS_TO_FEET),
                (int) Math.round(calories),
                round(activity.path("average_speed").asDouble(0) * 2.23694),
                (int) Math.round(avgPower),
                (int) Math.round(normalizedPower),
                FTP_WATTS,
                (int) Math.round(activity.path("average_heartrate").asDouble(0)),
                (int) Math.round(activity.path("average_cadence").asDouble(0)),
                round(tss),
                round(tss * 0.78),
                fatCalories,
                carbCalories,
                powerZones(avgPower),
                heartRateZones(activity.path("average_heartrate").asDouble(0)),
                List.of(summaryFromActivity(activity)));
    }

    private List<JsonNode> activities(CyclingPeriod period, int perPage) throws IOException, InterruptedException {
        String after = after(period).map(value -> "&after=" + value.getEpochSecond()).orElse("");
        JsonNode response = stravaApi("/athlete/activities?per_page=" + perPage + "&page=1" + after);
        List<JsonNode> activities = new ArrayList<>();
        for (JsonNode activity : response) {
            if (isRide(activity)) {
                activities.add(activity);
            }
        }
        activities.sort(Comparator.comparing(StravaCyclingService::instant).reversed());
        return activities;
    }

    private static java.util.Optional<Instant> after(CyclingPeriod period) {
        ZoneId zone = ZoneId.systemDefault();
        Instant now = Instant.now();
        return switch (period) {
            case TODAY -> java.util.Optional.of(LocalDate.now(zone).atStartOfDay(zone).toInstant());
            case WEEK -> java.util.Optional.of(now.minus(7, ChronoUnit.DAYS));
            case MONTH -> java.util.Optional.of(now.minus(30, ChronoUnit.DAYS));
            case YEAR -> java.util.Optional.of(now.minus(365, ChronoUnit.DAYS));
            case LIFETIME -> java.util.Optional.empty();
        };
    }

    private static boolean isRide(JsonNode activity) {
        String sportType = activity.path("sport_type").asText(activity.path("type").asText(""));
        return sportType.toLowerCase().contains("ride") || sportType.equalsIgnoreCase("VirtualRide");
    }

    private RideSummaryResponse summaryFromActivity(JsonNode activity) {
        return new RideSummaryResponse(
                activity.path("id").asText(),
                activity.path("name").asText("Untitled ride"),
                instant(activity),
                round(activity.path("distance").asDouble(0) * METERS_TO_MILES),
                round(activity.path("total_elevation_gain").asDouble(0) * METERS_TO_FEET));
    }

    private static CyclingSummaryResponse emptySummary(CyclingPeriod period) {
        return new CyclingSummaryResponse(period, 0, 0, 0, 0, 0, 0, 0, 0, FTP_WATTS, 0, 0, 0, 0, 0, 0,
                evenZones("Z1", "Z2", "Z3", "Z4", "Z5"),
                evenZones("Easy", "Endurance", "Tempo", "Threshold", "Max"),
                List.of());
    }

    private static List<ChartPointResponse> chart(JsonNode streams, String timeKey, String valueKey) {
        JsonNode time = streams.path(timeKey).path("data");
        JsonNode values = streams.path(valueKey).path("data");
        if (!time.isArray() || !values.isArray()) {
            return List.of();
        }
        List<ChartPointResponse> points = new ArrayList<>();
        int limit = Math.min(time.size(), values.size());
        int step = Math.max(1, limit / 80);
        for (int index = 0; index < limit; index += step) {
            if (values.get(index).isNumber()) {
                points.add(new ChartPointResponse(label(time.get(index).asInt()), values.get(index).asDouble()));
            }
        }
        return points;
    }

    private static List<ChartPointResponse> altitudeChart(JsonNode streams) {
        JsonNode time = streams.path("time").path("data");
        JsonNode values = streams.path("altitude").path("data");
        if (!time.isArray() || !values.isArray()) {
            return List.of();
        }
        List<ChartPointResponse> points = new ArrayList<>();
        int limit = Math.min(time.size(), values.size());
        int step = Math.max(1, limit / 80);
        for (int index = 0; index < limit; index += step) {
            if (values.get(index).isNumber()) {
                points.add(new ChartPointResponse(label(time.get(index).asInt()), values.get(index).asDouble() * METERS_TO_FEET));
            }
        }
        return points;
    }

    private static List<ZoneBucketResponse> powerZones(double averagePower) {
        if (averagePower <= 0) {
            return evenZones("Z1", "Z2", "Z3", "Z4", "Z5");
        }
        double ratio = averagePower / FTP_WATTS;
        if (ratio < 0.55) {
            return List.of(new ZoneBucketResponse("Z1", 62), new ZoneBucketResponse("Z2", 25), new ZoneBucketResponse("Z3", 8), new ZoneBucketResponse("Z4", 4), new ZoneBucketResponse("Z5", 1));
        }
        if (ratio < 0.76) {
            return List.of(new ZoneBucketResponse("Z1", 18), new ZoneBucketResponse("Z2", 52), new ZoneBucketResponse("Z3", 20), new ZoneBucketResponse("Z4", 8), new ZoneBucketResponse("Z5", 2));
        }
        if (ratio < 0.9) {
            return List.of(new ZoneBucketResponse("Z1", 10), new ZoneBucketResponse("Z2", 24), new ZoneBucketResponse("Z3", 42), new ZoneBucketResponse("Z4", 18), new ZoneBucketResponse("Z5", 6));
        }
        return List.of(new ZoneBucketResponse("Z1", 6), new ZoneBucketResponse("Z2", 14), new ZoneBucketResponse("Z3", 24), new ZoneBucketResponse("Z4", 38), new ZoneBucketResponse("Z5", 18));
    }

    private static List<ZoneBucketResponse> heartRateZones(double averageHeartRate) {
        if (averageHeartRate <= 0) {
            return evenZones("Easy", "Endurance", "Tempo", "Threshold", "Max");
        }
        if (averageHeartRate < 120) {
            return List.of(new ZoneBucketResponse("Easy", 58), new ZoneBucketResponse("Endurance", 28), new ZoneBucketResponse("Tempo", 10), new ZoneBucketResponse("Threshold", 3), new ZoneBucketResponse("Max", 1));
        }
        if (averageHeartRate < 145) {
            return List.of(new ZoneBucketResponse("Easy", 20), new ZoneBucketResponse("Endurance", 48), new ZoneBucketResponse("Tempo", 22), new ZoneBucketResponse("Threshold", 8), new ZoneBucketResponse("Max", 2));
        }
        if (averageHeartRate < 160) {
            return List.of(new ZoneBucketResponse("Easy", 8), new ZoneBucketResponse("Endurance", 24), new ZoneBucketResponse("Tempo", 42), new ZoneBucketResponse("Threshold", 20), new ZoneBucketResponse("Max", 6));
        }
        return List.of(new ZoneBucketResponse("Easy", 4), new ZoneBucketResponse("Endurance", 12), new ZoneBucketResponse("Tempo", 26), new ZoneBucketResponse("Threshold", 40), new ZoneBucketResponse("Max", 18));
    }

    private static List<ZoneBucketResponse> evenZones(String... labels) {
        return List.of(
                new ZoneBucketResponse(labels[0], 20),
                new ZoneBucketResponse(labels[1], 20),
                new ZoneBucketResponse(labels[2], 20),
                new ZoneBucketResponse(labels[3], 20),
                new ZoneBucketResponse(labels[4], 20));
    }

    private double calories(JsonNode activity) {
        if (activity.hasNonNull("calories")) {
            return activity.path("calories").asDouble();
        }
        if (activity.hasNonNull("kilojoules")) {
            return activity.path("kilojoules").asDouble();
        }
        double movingHours = activity.path("moving_time").asDouble(0) / 3600.0;
        double speedMph = activity.path("average_speed").asDouble(0) * 2.23694;
        return movingHours * Math.max(350, speedMph * 42);
    }

    private JsonNode stravaApi(String path) throws IOException, InterruptedException {
        ObjectNode token = freshToken();
        HttpRequest request = HttpRequest.newBuilder(URI.create(API_BASE + path))
                .header("Authorization", "Bearer " + token.path("access_token").asText())
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode data = response.body().isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(response.body());
        if (response.statusCode() >= 400) {
            throw new IllegalStateException(data.path("message").asText("Strava API request failed with " + response.statusCode()));
        }
        return data;
    }

    private boolean configured() {
        return properties.stravaClientId() != null && !properties.stravaClientId().isBlank()
                && properties.stravaClientSecret() != null && !properties.stravaClientSecret().isBlank();
    }

    private ObjectNode freshToken() throws IOException, InterruptedException {
        JsonNode token = readToken();
        if (token == null || !token.hasNonNull("access_token")) {
            throw new IllegalStateException("Connect to Strava first.");
        }
        long expiresAtMs = token.path("expires_at").asLong(0) * 1000;
        if (System.currentTimeMillis() < expiresAtMs - 60_000) {
            return (ObjectNode) token;
        }
        JsonNode refreshed = tokenRequest(Map.of(
                "client_id", properties.stravaClientId(),
                "client_secret", properties.stravaClientSecret(),
                "grant_type", "refresh_token",
                "refresh_token", token.path("refresh_token").asText()));
        ObjectNode merged = ((ObjectNode) token).deepCopy();
        merged.setAll((ObjectNode) refreshed);
        saveToken(merged);
        return merged;
    }

    private JsonNode tokenRequest(Map<String, String> params) throws IOException, InterruptedException {
        String body = params.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(java.util.stream.Collectors.joining("&"));
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://www.strava.com/oauth/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode data = objectMapper.readTree(response.body());
        if (response.statusCode() >= 400) {
            throw new IllegalStateException(data.path("message").asText("Strava token request failed."));
        }
        return data;
    }

    private JsonNode readToken() {
        try {
            Path path = Path.of(properties.tokenFile());
            if (!Files.exists(path)) {
                return null;
            }
            return objectMapper.readTree(Files.readString(path));
        } catch (Exception ignored) {
            return null;
        }
    }

    private void saveToken(JsonNode token) throws IOException {
        Path path = Path.of(properties.tokenFile());
        Files.createDirectories(path.getParent());
        Files.writeString(path, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(token));
    }

    private static Instant instant(JsonNode activity) {
        return Instant.parse(activity.path("start_date").asText(Instant.EPOCH.toString()));
    }

    private static String label(int seconds) {
        int minutes = seconds / 60;
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    private static double weighted(int weight, double value) {
        return weight > 0 ? value / weight : 0;
    }

    private static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
