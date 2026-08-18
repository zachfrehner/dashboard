package com.burnmetrix.dashboard.system;

public record SystemStatusResponse(
        Double cpuTemperatureC,
        Double load1,
        Double load5,
        Double load15,
        Long memoryTotalMb,
        Long memoryUsedMb,
        Double memoryUsedPercent,
        Long diskTotalGb,
        Long diskUsedGb,
        Double diskUsedPercent,
        Long uptimeSeconds,
        String ipAddress,
        String backendStatus,
        String kioskStatus,
        String nginxStatus) {
}
