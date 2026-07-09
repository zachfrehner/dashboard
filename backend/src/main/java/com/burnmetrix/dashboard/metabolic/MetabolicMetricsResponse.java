package com.burnmetrix.dashboard.metabolic;

public record MetabolicMetricsResponse(
        double fatCalories,
        double carbohydrateCalories,
        double totalCalories,
        Double maxPower,
        double gramsOfFatBurned,
        double gramsOfCarbohydrateBurned,
        double fatOxidationRateGPerHr,
        double carbOxidationRateGPerHr,
        double estimatedGlycogenDepletion,
        double remainingGlycogen,
        String timeUntilBonk,
        String fuelingRecommendation,
        double fuelDeficitOverRide,
        Double efficiencyDrift,
        Double heartRateDecoupling,
        String aerobicEfficiency,
        int metabolicFlexibilityScore,
        String personalizedZone2Range) {
}
