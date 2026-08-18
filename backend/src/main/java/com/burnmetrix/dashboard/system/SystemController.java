package com.burnmetrix.dashboard.system;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final KioskService kioskService;
    private final SystemStatusService systemStatusService;

    public SystemController(KioskService kioskService, SystemStatusService systemStatusService) {
        this.kioskService = kioskService;
        this.systemStatusService = systemStatusService;
    }

    @GetMapping("/status")
    public SystemStatusResponse status() {
        return systemStatusService.currentStatus();
    }

    @PostMapping("/kiosk/close")
    public ResponseEntity<SystemActionResponse> closeKiosk() {
        boolean requested = kioskService.closeKioskBrowser();
        return ResponseEntity.ok(new SystemActionResponse(requested, "Kiosk close requested"));
    }
}
