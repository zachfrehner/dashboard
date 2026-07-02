package com.burnmetrix.dashboard.cycling;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cycling_rides")
public class CyclingRideEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id")
    private String externalId;

    @Column(nullable = false)
    private String name;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "distance_miles", nullable = false)
    private double distanceMiles;

    @Column(name = "moving_time_seconds", nullable = false)
    private int movingTimeSeconds;

    @Column(name = "elapsed_time_seconds", nullable = false)
    private int elapsedTimeSeconds;

    @Column(name = "elevation_feet", nullable = false)
    private double elevationFeet;

    @Column(name = "average_speed_mph", nullable = false)
    private double averageSpeedMph;

    @Column(name = "average_power_watts")
    private Integer averagePowerWatts;

    @Column(name = "normalized_power_watts")
    private Integer normalizedPowerWatts;

    @Column(name = "average_heart_rate_bpm")
    private Integer averageHeartRateBpm;

    @Column(name = "average_cadence_rpm")
    private Integer averageCadenceRpm;

    private Integer calories;
    private Double tss;
    private String notes;

    @Column(nullable = false)
    private String source;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CyclingRideEntity() {
    }
}

