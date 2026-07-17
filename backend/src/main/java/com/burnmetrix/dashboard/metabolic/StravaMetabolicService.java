package com.burnmetrix.dashboard.metabolic;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class StravaMetabolicService implements MetabolicService {

    private static final DecimalFormat ONE_DECIMAL = new DecimalFormat("0.0");
    private static final DecimalFormat TWO_DECIMALS = new DecimalFormat("0.00");
    private static final String API_BASE = "https://www.strava.com/api/v3";

    private final MetabolicProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public StravaMetabolicService(MetabolicProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public String authorizationUrl() {
        assertConfigured();
        return "https://www.strava.com/oauth/authorize?client_id=" + encode(properties.stravaClientId())
                + "&redirect_uri=" + encode(properties.stravaRedirectUri())
                + "&response_type=code&approval_prompt=auto&scope=read,activity:read_all,activity:write";
    }

    @Override
    public void completeAuthorization(String code) throws IOException, InterruptedException {
        assertConfigured();
        JsonNode token = tokenRequest(Map.of(
                "client_id", properties.stravaClientId(),
                "client_secret", properties.stravaClientSecret(),
                "code", code,
                "grant_type", "authorization_code"));
        saveToken(token);
    }

    @Override
    public MetabolicStatusResponse status() {
        JsonNode token = readToken();
        JsonNode athlete = token == null ? null : token.path("athlete");
        AthleteResponse athleteResponse = athlete == null || athlete.isMissingNode() || athlete.isNull()
                ? null
                : new AthleteResponse(athlete.path("id").asLong(), athlete.path("firstname").asText(""), athlete.path("lastname").asText(""));
        return new MetabolicStatusResponse(properties.configured(), token != null && token.hasNonNull("access_token"), athleteResponse);
    }

    @Override
    public List<MetabolicActivityResponse> activities() throws IOException, InterruptedException {
        JsonNode activities = stravaApi("/athlete/activities?per_page=5&page=1", "GET", null);
        List<MetabolicActivityResponse> responses = new ArrayList<>();
        for (JsonNode activity : activities) {
            responses.add(new MetabolicActivityResponse(
                    activity.path("id").asLong(),
                    activity.path("name").asText("Untitled ride"),
                    activity.path("sport_type").asText(activity.path("type").asText("Ride")),
                    activity.path("start_date_local").asText(""),
                    activity.path("moving_time").asInt(),
                    activity.path("distance").asDouble(),
                    activity.path("has_heartrate").asBoolean(false),
                    nullableDouble(activity, "average_heartrate"),
                    nullableDouble(activity, "max_heartrate")));
        }
        return responses;
    }

    @Override
    public MetabolicAnalysisResponse analyze(String activityId) throws IOException, InterruptedException {
        JsonNode activity = stravaApi("/activities/" + activityId, "GET", null);
        JsonNode streams = stravaApi("/activities/" + activityId + "/streams?keys=time,heartrate,watts,velocity_smooth,distance&key_by_type=true", "GET", null);
        List<LabRow> labRows = readLabRows();
        JsonNode time = streams.path("time").path("data");
        JsonNode heartRate = streams.path("heartrate").path("data");

        if (!time.isArray() || !heartRate.isArray() || time.size() < 2 || heartRate.size() < 2) {
            throw new IllegalStateException("This activity does not have enough Strava heart-rate stream data to analyze.");
        }

        JsonNode watts = streams.path("watts").path("data");
        JsonNode velocity = streams.path("velocity_smooth").path("data");
        List<Sample> samples = new ArrayList<>();
        Double maxPower = nullableDouble(activity, "max_watts");
        double fatGrams = 0;
        double carbGrams = 0;
        double totalMinutes = 0;
        int limit = Math.min(time.size(), heartRate.size());

        for (int index = 1; index < limit; index++) {
            double minutes = Math.max(0, (time.get(index).asDouble() - time.get(index - 1).asDouble()) / 60.0);
            if (minutes == 0) {
                continue;
            }
            double hr = heartRate.get(index).asDouble();
            Double power = watts.isArray() && watts.size() > index && watts.get(index).isNumber() ? watts.get(index).asDouble() : null;
            Double speed = velocity.isArray() && velocity.size() > index && velocity.get(index).isNumber() ? velocity.get(index).asDouble() : null;
            if (power != null) {
                maxPower = Math.max(maxPower == null ? 0 : maxPower, power);
            }
            LabRow rates = interpolateLab(labRows, hr);
            fatGrams += rates.fatGPerMin() * minutes;
            carbGrams += rates.carbGPerMin() * minutes;
            totalMinutes += minutes;
            samples.add(new Sample(time.get(index).asDouble(), hr, power, speed, rates.fatGPerMin(), rates.carbGPerMin()));
        }

        MetabolicMetricsResponse metrics = metrics(fatGrams, carbGrams, totalMinutes, maxPower, samples, labRows);
        return new MetabolicAnalysisResponse(
                new MetabolicActivityDetailResponse(
                        activity.path("id").asLong(),
                        activity.path("name").asText("Untitled ride"),
                        activity.path("description").asText(""),
                        activity.path("moving_time").asInt(),
                        activity.path("distance").asDouble()),
                metrics,
                formatReport(metrics),
                downsample(samples),
                samples.size(),
                labRows.size());
    }

    @Override
    public void updateDescription(String activityId, String report) throws IOException, InterruptedException {
        if (report == null || report.isBlank()) {
            throw new IllegalArgumentException("Missing report text.");
        }
        JsonNode activity = stravaApi("/activities/" + activityId, "GET", null);
        String existing = activity.path("description").asText("");
        String markerStart = "[Metabolic analysis]";
        String markerEnd = "[/Metabolic analysis]";
        String stripped = existing.replaceAll("(?s)\\n?\\Q" + markerStart + "\\E.*?\\Q" + markerEnd + "\\E", "").trim();
        String description = (stripped.isBlank() ? "" : stripped + "\n\n") + markerStart + "\n" + report + "\n" + markerEnd;
        stravaApi("/activities/" + activityId, "PUT", "description=" + encode(description));
    }

    private MetabolicMetricsResponse metrics(double fatGrams, double carbGrams, double totalMinutes, Double maxPower, List<Sample> samples, List<LabRow> labRows) {
        double fatCalories = fatGrams * 9;
        double carbCalories = carbGrams * 4;
        double totalCalories = fatCalories + carbCalories;
        double durationHours = totalMinutes / 60.0;
        double plannedCarbs = properties.plannedCarbIntakeGPerHour() * durationHours;
        double fuelDeficit = Math.max(0, carbGrams - plannedCarbs);
        double remainingGlycogen = Math.max(0, properties.glycogenStartGrams() - fuelDeficit);
        double netCarbBurnPerHour = Math.max(0, (carbGrams / Math.max(durationHours, 0.01)) - properties.plannedCarbIntakeGPerHour());
        double hoursUntilBonk = netCarbBurnPerHour > 0 ? remainingGlycogen / netCarbBurnPerHour : Double.POSITIVE_INFINITY;
        int eatInMinutes = fuelDeficit > 20 ? 0 : Math.max(0, (int) Math.round((20 - fuelDeficit) / Math.max(netCarbBurnPerHour / 60.0, 0.1)));
        Drift drift = calculateDrift(samples);
        Zone2 zone2 = calculateZone2(labRows);
        double fatShare = totalCalories > 0 ? fatCalories / totalCalories : 0;
        int flexibility = (int) Math.round(Math.max(0, Math.min(100, (fatShare * 70) + (zone2.widthScore() * 30))));

        return new MetabolicMetricsResponse(
                fatCalories,
                carbCalories,
                totalCalories,
                maxPower,
                fatGrams,
                carbGrams,
                durationHours > 0 ? fatGrams / durationHours : 0,
                durationHours > 0 ? carbGrams / durationHours : 0,
                Math.min(properties.glycogenStartGrams(), fuelDeficit),
                remainingGlycogen,
                Double.isFinite(hoursUntilBonk) ? ONE_DECIMAL.format(hoursUntilBonk) + " hr" : "Not projected at current fueling",
                eatInMinutes == 0 ? "Eat now" : "Eat in " + eatInMinutes + " minutes",
                fuelDeficit,
                drift.efficiencyDriftPercent(),
                drift.heartRateDecouplingPercent(),
                drift.aerobicEfficiency(),
                flexibility,
                zone2.low() + "-" + zone2.high() + " bpm");
    }

    private List<LabRow> readLabRows() throws IOException {
        String csv = new String(new ClassPathResource("metabolic/vo2test.csv").getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        List<String> lines = csv.lines().filter(line -> !line.isBlank()).toList();
        String[] headers = splitCsvLine(lines.get(0)).stream().map(StravaMetabolicService::normalizeKey).toArray(String[]::new);
        List<LabRow> rows = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            List<String> values = splitCsvLine(lines.get(i));
            Map<String, String> row = new java.util.HashMap<>();
            for (int j = 0; j < headers.length; j++) {
                row.put(headers[j], j < values.size() ? values.get(j) : "");
            }
            double hr = numberFrom(row, List.of("heart_rate_bpm", "heart_rate", "hr_bpm", "hr", "bpm"));
            double fat = numberFrom(row, List.of("fat_g_per_min", "fat_g_min", "fat_oxidation_g_min"));
            double carb = numberFrom(row, List.of("carb_g_per_min", "carb_g_min", "carb_oxidation_g_min", "cho_g_per_min"));
            double fatCalPerMin = numberFrom(row, List.of("fat_cal_per_min", "fat_kcal_per_min"));
            double carbCalPerMin = numberFrom(row, List.of("carb_cal_per_min", "carb_kcal_per_min", "cho_cal_per_min"));
            double fatCalPerHour = numberFrom(row, List.of("fat_calories_kcal_hr", "fat_kcal_hr", "fat_cal_per_hr"));
            double carbCalPerHour = numberFrom(row, List.of("carb_calories_kcal_hr", "carb_kcal_hr", "carb_cal_per_hr", "cho_kcal_hr"));
            double totalCalPerHour = numberFrom(row, List.of("ree_kcal_hr", "total_kcal_hr", "kcal_hr", "energy_kcal_hr"));
            if (!Double.isFinite(fat) && Double.isFinite(fatCalPerMin)) {
                fat = fatCalPerMin / 9;
            }
            if (!Double.isFinite(carb) && Double.isFinite(carbCalPerMin)) {
                carb = carbCalPerMin / 4;
            }
            if (!Double.isFinite(fat) && Double.isFinite(fatCalPerHour)) {
                fat = (fatCalPerHour / 60) / 9;
            }
            if (!Double.isFinite(carb) && Double.isFinite(carbCalPerHour)) {
                carb = (carbCalPerHour / 60) / 4;
            }
            if (!Double.isFinite(carb) && Double.isFinite(totalCalPerHour) && Double.isFinite(fatCalPerHour)) {
                carb = (Math.max(0, totalCalPerHour - fatCalPerHour) / 60) / 4;
            }
            if (Double.isFinite(hr) && Double.isFinite(fat) && Double.isFinite(carb)) {
                rows.add(new LabRow(hr, fat, carb));
            }
        }
        if (rows.size() < 2) {
            throw new IllegalStateException("Lab CSV needs at least two rows with heart rate, fat oxidation, and carb oxidation data.");
        }
        rows.sort(Comparator.comparingDouble(LabRow::hr));
        return rows;
    }

    private static LabRow interpolateLab(List<LabRow> rows, double hr) {
        if (hr <= rows.get(0).hr()) {
            return rows.get(0);
        }
        if (hr >= rows.get(rows.size() - 1).hr()) {
            return rows.get(rows.size() - 1);
        }
        for (int i = 1; i < rows.size(); i++) {
            LabRow high = rows.get(i);
            if (high.hr() >= hr) {
                LabRow low = rows.get(i - 1);
                double ratio = (hr - low.hr()) / (high.hr() - low.hr());
                return new LabRow(hr, low.fatGPerMin() + ((high.fatGPerMin() - low.fatGPerMin()) * ratio),
                        low.carbGPerMin() + ((high.carbGPerMin() - low.carbGPerMin()) * ratio));
            }
        }
        return rows.get(rows.size() - 1);
    }

    private static Drift calculateDrift(List<Sample> samples) {
        List<Sample> usable = samples.stream()
                .filter(sample -> Double.isFinite(sample.heartRate()) && (sample.watts() != null || sample.velocity() != null))
                .toList();
        if (usable.size() < 20) {
            return new Drift(null, null, "Needs power or speed stream");
        }
        Segment first = summarize(usable.subList(0, usable.size() / 2));
        Segment second = summarize(usable.subList(usable.size() / 2, usable.size()));
        double firstOutput = first.avgWatts() != null ? first.avgWatts() : valueOrZero(first.avgVelocity());
        double secondOutput = second.avgWatts() != null ? second.avgWatts() : valueOrZero(second.avgVelocity());
        double firstRatio = first.avgHeartRate() / Math.max(firstOutput, 0.01);
        double secondRatio = second.avgHeartRate() / Math.max(secondOutput, 0.01);
        double decoupling = ((secondRatio - firstRatio) / Math.max(firstRatio, 0.01)) * 100;
        String efficiency = first.avgWatts() != null || second.avgWatts() != null
                ? TWO_DECIMALS.format(((valueOrZero(first.avgWatts()) + valueOrZero(second.avgWatts())) / 2) / ((first.avgHeartRate() + second.avgHeartRate()) / 2)) + " W/bpm"
                : new DecimalFormat("0.000").format(((valueOrZero(first.avgVelocity()) + valueOrZero(second.avgVelocity())) / 2) / ((first.avgHeartRate() + second.avgHeartRate()) / 2)) + " m/s/bpm";
        return new Drift(decoupling, decoupling, efficiency);
    }

    private static Segment summarize(List<Sample> samples) {
        return new Segment(
                average(samples.stream().mapToDouble(Sample::heartRate).boxed().toList()).orElse(0),
                nullableAverage(samples.stream().map(Sample::watts).toList()),
                nullableAverage(samples.stream().map(Sample::velocity).toList()));
    }

    private static Zone2 calculateZone2(List<LabRow> rows) {
        double maxFat = rows.stream().mapToDouble(LabRow::fatGPerMin).max().orElse(0);
        double threshold = maxFat * 0.75;
        List<LabRow> candidates = rows.stream().filter(row -> row.fatGPerMin() >= threshold).toList();
        int low = (int) Math.round(candidates.isEmpty() ? rows.get(0).hr() : candidates.get(0).hr());
        int high = (int) Math.round(candidates.isEmpty() ? rows.get(rows.size() - 1).hr() : candidates.get(candidates.size() - 1).hr());
        return new Zone2(low, high, Math.max(0, Math.min(1, (high - low) / 50.0)));
    }

    private static List<MetabolicChartSampleResponse> downsample(List<Sample> samples) {
        int maxPoints = 500;
        if (samples.size() <= maxPoints) {
            return samples.stream().map(StravaMetabolicService::chartPoint).toList();
        }
        int bucketSize = (int) Math.ceil(samples.size() / (double) maxPoints);
        List<MetabolicChartSampleResponse> points = new ArrayList<>();
        for (int index = 0; index < samples.size(); index += bucketSize) {
            List<Sample> bucket = samples.subList(index, Math.min(samples.size(), index + bucketSize));
            Sample mid = bucket.get(bucket.size() / 2);
            points.add(new MetabolicChartSampleResponse(
                    mid.second(),
                    average(bucket.stream().mapToDouble(Sample::heartRate).boxed().toList()).orElse(0),
                    average(bucket.stream().mapToDouble(Sample::fatGPerMin).boxed().toList()).orElse(0),
                    average(bucket.stream().mapToDouble(Sample::carbGPerMin).boxed().toList()).orElse(0)));
        }
        return points;
    }

    private static MetabolicChartSampleResponse chartPoint(Sample sample) {
        return new MetabolicChartSampleResponse(sample.second(), sample.heartRate(), sample.fatGPerMin(), sample.carbGPerMin());
    }

    private String formatReport(MetabolicMetricsResponse metrics) {
        return List.of(
                "Fat calories: " + whole(metrics.fatCalories()) + " kcal",
                "Carbohydrate calories: " + whole(metrics.carbohydrateCalories()) + " kcal",
                "Total calories: " + whole(metrics.totalCalories()) + " kcal",
                "Max power: " + (metrics.maxPower() == null ? "No power data" : whole(metrics.maxPower()) + " W"),
                "Grams of fat burned: " + ONE_DECIMAL.format(metrics.gramsOfFatBurned()) + " g",
                "Grams of carbohydrate burned: " + ONE_DECIMAL.format(metrics.gramsOfCarbohydrateBurned()) + " g",
                "Fat oxidation rate: " + ONE_DECIMAL.format(metrics.fatOxidationRateGPerHr()) + " g/hr",
                "Carb oxidation rate: " + ONE_DECIMAL.format(metrics.carbOxidationRateGPerHr()) + " g/hr",
                "Efficiency drift over long rides: " + percent(metrics.efficiencyDrift()),
                "Heart-rate decoupling: " + percent(metrics.heartRateDecoupling()),
                "Aerobic efficiency: " + metrics.aerobicEfficiency(),
                "Metabolic flexibility score: " + metrics.metabolicFlexibilityScore() + "/100",
                "Personalized Zone 2 range: " + metrics.personalizedZone2Range())
                .stream().collect(Collectors.joining("\n"));
    }

    private JsonNode stravaApi(String path, String method, String body) throws IOException, InterruptedException {
        ObjectNode token = freshToken();
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(API_BASE + path))
                .header("Authorization", "Bearer " + token.path("access_token").asText());
        if ("PUT".equals(method)) {
            builder.header("Content-Type", "application/x-www-form-urlencoded").PUT(HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
        } else {
            builder.GET();
        }
        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        JsonNode data = response.body().isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(response.body());
        if (response.statusCode() >= 400) {
            throw new IllegalStateException(data.path("message").asText("Strava API request failed with " + response.statusCode()));
        }
        return data;
    }

    private ObjectNode freshToken() throws IOException, InterruptedException {
        assertConfigured();
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
                .collect(Collectors.joining("&"));
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

    private void assertConfigured() {
        if (!properties.configured()) {
            throw new IllegalStateException("Add STRAVA_CLIENT_ID and STRAVA_CLIENT_SECRET first.");
        }
    }

    private static List<String> splitCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (character == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                current.append('"');
                i++;
            } else if (character == '"') {
                quoted = !quoted;
            } else if (character == ',' && !quoted) {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        values.add(current.toString().trim());
        return values;
    }

    private static double numberFrom(Map<String, String> row, List<String> keys) {
        for (String key : keys) {
            try {
                double value = Double.parseDouble(row.getOrDefault(normalizeKey(key), ""));
                if (Double.isFinite(value)) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return Double.NaN;
    }

    private static String normalizeKey(String key) {
        return key.trim().toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
    }

    private static OptionalDouble average(List<Double> values) {
        return values.stream().filter(value -> value != null && Double.isFinite(value)).mapToDouble(Double::doubleValue).average();
    }

    private static Double nullableAverage(List<Double> values) {
        OptionalDouble average = average(values);
        return average.isPresent() ? average.getAsDouble() : null;
    }

    private static Double nullableDouble(JsonNode node, String field) {
        return node.hasNonNull(field) && node.get(field).isNumber() ? node.get(field).asDouble() : null;
    }

    private static double valueOrZero(Double value) {
        return value == null ? 0 : value;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String whole(double value) {
        return new DecimalFormat("0").format(value);
    }

    private static String percent(Double value) {
        return value == null ? "Needs power or speed stream" : ONE_DECIMAL.format(value) + "%";
    }

    private record LabRow(double hr, double fatGPerMin, double carbGPerMin) {
    }

    private record Sample(double second, double heartRate, Double watts, Double velocity, double fatGPerMin, double carbGPerMin) {
    }

    private record Segment(double avgHeartRate, Double avgWatts, Double avgVelocity) {
    }

    private record Drift(Double efficiencyDriftPercent, Double heartRateDecouplingPercent, String aerobicEfficiency) {
    }

    private record Zone2(int low, int high, double widthScore) {
    }
}
