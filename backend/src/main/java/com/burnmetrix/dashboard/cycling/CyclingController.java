package com.burnmetrix.dashboard.cycling;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cycling")
public class CyclingController {

    private final CyclingService cyclingService;

    public CyclingController(CyclingService cyclingService) {
        this.cyclingService = cyclingService;
    }

    @GetMapping("/today")
    public CyclingSummaryResponse today() {
        return cyclingService.summary(CyclingPeriod.TODAY);
    }

    @GetMapping("/week")
    public CyclingSummaryResponse week() {
        return cyclingService.summary(CyclingPeriod.WEEK);
    }

    @GetMapping("/month")
    public CyclingSummaryResponse month() {
        return cyclingService.summary(CyclingPeriod.MONTH);
    }

    @GetMapping("/year")
    public CyclingSummaryResponse year() {
        return cyclingService.summary(CyclingPeriod.YEAR);
    }

    @GetMapping("/lifetime")
    public CyclingSummaryResponse lifetime() {
        return cyclingService.summary(CyclingPeriod.LIFETIME);
    }

    @GetMapping("/rides/{rideId}")
    public RideDetailResponse ride(@PathVariable String rideId) {
        return cyclingService.rideDetail(rideId);
    }
}
