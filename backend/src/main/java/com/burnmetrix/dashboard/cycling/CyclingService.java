package com.burnmetrix.dashboard.cycling;

public interface CyclingService {

    CyclingSummaryResponse summary(CyclingPeriod period);

    RideDetailResponse rideDetail(String rideId);
}

