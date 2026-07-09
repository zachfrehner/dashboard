package com.burnmetrix.dashboard.system;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final KioskService kioskService;

    public SystemController(KioskService kioskService) {
        this.kioskService = kioskService;
    }

    @PostMapping("/kiosk/close")
    public ResponseEntity<SystemActionResponse> closeKiosk() {
        boolean requested = kioskService.closeKioskBrowser();
        return ResponseEntity.ok(new SystemActionResponse(requested, "Kiosk close requested"));
    }
}
