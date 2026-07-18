package com.burnmetrix.dashboard.cycling;

import java.util.List;

public record CyclingSummaryResponse(
        CyclingPeriod period,
        Double distanceMiles,
        Integer rideTimeSeconds,
        Integer movingTimeSeconds,
        Double elevationFeet,
        Integer calories,
        Double averageSpeedMph,
        Integer averagePowerWatts,
        Integer normalizedPowerWatts,
        Integer ftpWatts,
        Integer averageHeartRateBpm,
        Integer averageCadenceRpm,
        Double tss,
        Double trainingLoad,
        Integer fatCalories,
        Integer carbCalories,
        List<ZoneBucketResponse> powerZones,
        List<ZoneBucketResponse> heartRateZones,
        List<RideSummaryResponse> recentRides) {
}
