package com.burnmetrix.dashboard.metabolic;

public record MetabolicActivityResponse(
        long id,
        String name,
        String sportType,
        String startDateLocal,
        int movingTime,
        double distance,
        boolean hasHeartrate,
        Double averageHeartrate,
        Double maxHeartrate) {
}
