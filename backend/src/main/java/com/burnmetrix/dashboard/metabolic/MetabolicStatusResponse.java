package com.burnmetrix.dashboard.metabolic;

public record MetabolicStatusResponse(boolean configured, boolean connected, AthleteResponse athlete) {
}
