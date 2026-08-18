package com.burnmetrix.dashboard.system;

import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

@Service
public class LocalSystemStatusService implements SystemStatusService {

    @Override
    public SystemStatusResponse currentStatus() {
        Load load = load();
        Memory memory = memory();
        Disk disk = disk();
        return new SystemStatusResponse(
                cpuTemperatureC(),
                load.load1(),
                load.load5(),
                load.load15(),
                memory.totalMb(),
                memory.usedMb(),
                memory.usedPercent(),
                disk.totalGb(),
                disk.usedGb(),
                disk.usedPercent(),
                uptimeSeconds(),
                ipAddress(),
                serviceStatus("burnmetrix-backend"),
                serviceStatus("burnmetrix-kiosk"),
                serviceStatus("nginx"));
    }

    private static Double cpuTemperatureC() {
        try {
            Path path = Path.of("/sys/class/thermal/thermal_zone0/temp");
            if (!Files.exists(path)) {
                return null;
            }
            return round(Double.parseDouble(Files.readString(path).trim()) / 1000.0);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Load load() {
        try {
            Path path = Path.of("/proc/loadavg");
            if (!Files.exists(path)) {
                return new Load(null, null, null);
            }
            String[] parts = Files.readString(path).trim().split("\\s+");
            return new Load(parseDouble(parts, 0), parseDouble(parts, 1), parseDouble(parts, 2));
        } catch (Exception ignored) {
            return new Load(null, null, null);
        }
    }

    private static Memory memory() {
        try {
            Path path = Path.of("/proc/meminfo");
            if (!Files.exists(path)) {
                return new Memory(null, null, null);
            }
            Map<String, Long> values = new HashMap<>();
            for (String line : Files.readAllLines(path)) {
                String[] parts = line.split(":");
                if (parts.length < 2) {
                    continue;
                }
                String number = parts[1].replaceAll("[^0-9]", "");
                if (!number.isBlank()) {
                    values.put(parts[0], Long.parseLong(number));
                }
            }
            Long totalKb = values.get("MemTotal");
            Long availableKb = values.get("MemAvailable");
            if (totalKb == null || availableKb == null || totalKb == 0) {
                return new Memory(null, null, null);
            }
            long usedKb = totalKb - availableKb;
            return new Memory(totalKb / 1024, usedKb / 1024, round(usedKb * 100.0 / totalKb));
        } catch (Exception ignored) {
            return new Memory(null, null, null);
        }
    }

    private static Disk disk() {
        try {
            FileStore store = Files.getFileStore(Path.of(".").toAbsolutePath());
            long total = store.getTotalSpace();
            long used = total - store.getUsableSpace();
            if (total <= 0) {
                return new Disk(null, null, null);
            }
            long gb = 1024L * 1024L * 1024L;
            return new Disk(total / gb, used / gb, round(used * 100.0 / total));
        } catch (Exception ignored) {
            return new Disk(null, null, null);
        }
    }

    private static Long uptimeSeconds() {
        try {
            Path path = Path.of("/proc/uptime");
            if (!Files.exists(path)) {
                return null;
            }
            String first = Files.readString(path).trim().split("\\s+")[0];
            return (long) Double.parseDouble(first);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String ipAddress() {
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }
                for (var address : Collections.list(networkInterface.getInetAddresses())) {
                    if (address instanceof Inet4Address inet4Address && !inet4Address.isLoopbackAddress()) {
                        return inet4Address.getHostAddress();
                    }
                }
            }
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String serviceStatus(String service) {
        if (!isLinux()) {
            return null;
        }
        try {
            Process process = new ProcessBuilder("systemctl", "is-active", service).start();
            boolean finished = process.waitFor(2, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return null;
            }
            String output = new String(process.getInputStream().readAllBytes()).trim();
            return output.isBlank() ? null : output;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Double parseDouble(String[] values, int index) {
        try {
            return Double.parseDouble(values[index]);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static boolean isLinux() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("linux");
    }

    private record Load(Double load1, Double load5, Double load15) {
    }

    private record Memory(Long totalMb, Long usedMb, Double usedPercent) {
    }

    private record Disk(Long totalGb, Long usedGb, Double usedPercent) {
    }
}
