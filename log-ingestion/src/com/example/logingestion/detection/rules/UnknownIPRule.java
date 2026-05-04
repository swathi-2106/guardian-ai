package com.example.logingestion.detection.rules;

import com.example.logingestion.LogEntry;
import com.example.logingestion.detection.Alert;
import com.example.logingestion.detection.DetectionRule;

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
            if (!KNOWN_IPS.contains(log.getIpAddress())) {
                alerts.add(new Alert(
                        log.getTimestamp(),
                        "MEDIUM",
                        "Access from unknown IP: " + log.getIpAddress(),
                        Collections.singletonList(log)
                ));
            }
        }

        return alerts;
    }
}