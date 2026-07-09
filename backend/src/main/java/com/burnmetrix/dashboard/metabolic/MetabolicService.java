package com.burnmetrix.dashboard.metabolic;

import java.io.IOException;
import java.util.List;

public interface MetabolicService {

    String authorizationUrl();

    void completeAuthorization(String code) throws IOException, InterruptedException;

    MetabolicStatusResponse status();

    List<MetabolicActivityResponse> activities() throws IOException, InterruptedException;

    MetabolicAnalysisResponse analyze(String activityId) throws IOException, InterruptedException;

    void updateDescription(String activityId, String report) throws IOException, InterruptedException;
}
