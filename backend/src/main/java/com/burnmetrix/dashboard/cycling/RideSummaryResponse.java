package com.burnmetrix.dashboard.cycling;

import java.time.Instant;

public record RideSummaryResponse(
        String id,
        String name,
        Instant startedAt,
        double distanceMiles,
        double elevationFeet) {
}

