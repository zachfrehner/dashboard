package com.burnmetrix.dashboard.system;

import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class LocalKioskService implements KioskService {

    @Override
    public boolean closeKioskBrowser() {
        if (!isLinux()) {
            return false;
        }

        try {
            new ProcessBuilder("pkill", "-f", "chromium.*localhost").start();
            new ProcessBuilder("pkill", "-f", "chromium-browser.*localhost").start();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean isLinux() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("linux");
    }
}
