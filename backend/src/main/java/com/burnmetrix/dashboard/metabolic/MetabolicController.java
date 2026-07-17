package com.burnmetrix.dashboard.metabolic;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metabolic")
public class MetabolicController {

    private final MetabolicService metabolicService;

    public MetabolicController(MetabolicService metabolicService) {
        this.metabolicService = metabolicService;
    }

    @GetMapping("/auth/start")
    public ResponseEntity<Void> startAuth() {
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, metabolicService.authorizationUrl())
                .build();
    }

    @GetMapping("/auth/callback")
    public ResponseEntity<Void> callback(@RequestParam String code, HttpServletRequest request) throws IOException, InterruptedException {
        metabolicService.completeAuthorization(code);
        String host = request.getServerName() == null || request.getServerName().isBlank()
                ? "localhost"
                : request.getServerName();
        return ResponseEntity.status(302).location(URI.create(request.getScheme() + "://" + host + "/calories?connected=1")).build();
    }

    @GetMapping("/status")
    public MetabolicStatusResponse status() {
        return metabolicService.status();
    }

    @GetMapping("/activities")
    public List<MetabolicActivityResponse> activities() throws IOException, InterruptedException {
        return metabolicService.activities();
    }

    @GetMapping("/analyze/{activityId}")
    public MetabolicAnalysisResponse analyze(@PathVariable String activityId) throws IOException, InterruptedException {
        return metabolicService.analyze(activityId);
    }

    @PostMapping("/update-description/{activityId}")
    public Map<String, Object> updateDescription(@PathVariable String activityId, @RequestBody UpdateDescriptionRequest request)
            throws IOException, InterruptedException {
        metabolicService.updateDescription(activityId, request.report());
        return Map.of("ok", true);
    }
}
