package com.burnmetrix.dashboard.metabolic;

import java.util.List;

public record MetabolicAnalysisResponse(
        MetabolicActivityDetailResponse activity,
        MetabolicMetricsResponse metrics,
        String report,
        List<MetabolicChartSampleResponse> chartSamples,
        int sampleCount,
        int labRows) {
}
