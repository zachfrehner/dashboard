package com.burnmetrix.dashboard.metabolic;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "burnmetrix.metabolic")
public record MetabolicProperties(
        String stravaClientId,
        String stravaClientSecret,
        String stravaRedirectUri,
        String tokenFile,
        double glycogenStartGrams,
        double plannedCarbIntakeGPerHour) {

    boolean configured() {
        return stravaClientId != null && !stravaClientId.isBlank()
                && stravaClientSecret != null && !stravaClientSecret.isBlank();
    }
}
