package com.burnmetrix.dashboard.cycling;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class MockCyclingService implements CyclingService {

    @Override
    public CyclingSummaryResponse summary(CyclingPeriod period) {
        double multiplier = switch (period) {
            case TODAY -> 1.0;
            case WEEK -> 4.4;
            case MONTH -> 17.8;
            case YEAR -> 211.0;
            case LIFETIME -> 1840.0;
        };

        return new CyclingSummaryResponse(
                period,
                round(24.7 * multiplier),
                (int) (5_820 * multiplier),
                (int) (5_240 * multiplier),
                round(1_120 * multiplier),
                (int) (820 * multiplier),
                16.9,
                184,
                206,
                255,
                142,
                86,
                round(61.5 * multiplier),
                round(48.0 * multiplier),
                (int) (305 * multiplier),
                (int) (515 * multiplier),
                zones("Z1", "Z2", "Z3", "Z4", "Z5"),
                zones("Easy", "Endurance", "Tempo", "Threshold", "Max"),
                recentRides());
    }

    @Override
    public RideDetailResponse rideDetail(String rideId) {
        return new RideDetailResponse(
                rideId,
                "Foothills tempo loop",
                Instant.now().minus(1, ChronoUnit.DAYS),
                summary(CyclingPeriod.TODAY),
                List.of(
                        new ChartPointResponse("00:00", 142),
                        new ChartPointResponse("00:20", 188),
                        new ChartPointResponse("00:40", 214),
                        new ChartPointResponse("01:00", 176)),
                List.of(
                        new ChartPointResponse("00:00", 118),
                        new ChartPointResponse("00:20", 142),
                        new ChartPointResponse("00:40", 151),
                        new ChartPointResponse("01:00", 136)),
                List.of(
                        new ChartPointResponse("00:00", 4_820),
                        new ChartPointResponse("00:20", 5_120),
                        new ChartPointResponse("00:40", 5_540),
                        new ChartPointResponse("01:00", 5_080)),
                "Mock BurnMetrix estimate: balanced endurance burn with a moderate carbohydrate load.",
                "Sample ride detail; replace with imported Strava, FIT, or Garmin data later.");
    }

    private static List<ZoneBucketResponse> zones(String... labels) {
        return List.of(
                new ZoneBucketResponse(labels[0], 18),
                new ZoneBucketResponse(labels[1], 34),
                new ZoneBucketResponse(labels[2], 26),
                new ZoneBucketResponse(labels[3], 14),
                new ZoneBucketResponse(labels[4], 8));
    }

    private static List<RideSummaryResponse> recentRides() {
        return List.of(
                new RideSummaryResponse("ride-1", "Foothills tempo loop", Instant.now().minus(1, ChronoUnit.DAYS), 24.7, 1_120),
                new RideSummaryResponse("ride-2", "Recovery spin", Instant.now().minus(3, ChronoUnit.DAYS), 15.2, 320));
    }

    private static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}

