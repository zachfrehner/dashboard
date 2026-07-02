package com.burnmetrix.dashboard.cycling;

import java.util.List;

public record CyclingSummaryResponse(
        CyclingPeriod period,
        double distanceMiles,
        int rideTimeSeconds,
        int movingTimeSeconds,
        double elevationFeet,
        int calories,
        double averageSpeedMph,
        int averagePowerWatts,
        int normalizedPowerWatts,
        int ftpWatts,
        int averageHeartRateBpm,
        int averageCadenceRpm,
        double tss,
        double trainingLoad,
        int fatCalories,
        int carbCalories,
        List<ZoneBucketResponse> powerZones,
        List<ZoneBucketResponse> heartRateZones,
        List<RideSummaryResponse> recentRides) {
}

