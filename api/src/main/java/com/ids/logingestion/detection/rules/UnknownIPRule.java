package com.ids.logingestion.detection.rules;

import com.ids.logingestion.LogEntry;
import com.ids.logingestion.detection.Alert;
import com.ids.logingestion.detection.DetectionRule;

import java.util.*;

public class UnknownIPRule implements DetectionRule {

    private static final Set<String> KNOWN_IPS = new HashSet<>(Arrays.asList(
            "192.168.1.1",
            "192.168.1.2"
    ));

    @Override
    public List<Alert> apply(List<LogEntry> logs) {
        List<Alert> alerts = new ArrayList<>();

        for (LogEntry log : logs) {
            String ipAddress = log.getIpAddress();
            if (ipAddress == null || ipAddress.isBlank() || ipAddress.equalsIgnoreCase("LOCAL")) {
                continue;
            }

            if (!KNOWN_IPS.contains(ipAddress)) {
                alerts.add(new Alert(
                        log.getTimestamp(),
                        "MEDIUM",
                        "Access from unknown IP: " + ipAddress,
                        Collections.singletonList(log)
                ));
            }
        }

        return alerts;
    }
}
