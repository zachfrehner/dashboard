package com.burnmetrix.dashboard.cycling;

import java.time.Instant;
import java.util.List;

public record RideDetailResponse(
        String id,
        String name,
        Instant startedAt,
        CyclingSummaryResponse summary,
        List<ChartPointResponse> power,
        List<ChartPointResponse> heartRate,
        List<ChartPointResponse> elevation,
        String burnMetrixSummary,
        String notes) {
}

