package com.example.logingestion.detection.rules;

import com.example.logingestion.LogEntry;
import com.example.logingestion.detection.Alert;
import com.example.logingestion.detection.DetectionRule;

import java.util.*;

public class MultipleFailedLoginRule implements DetectionRule {

    private static final int THRESHOLD = 3;

    @Override
    public List<Alert> apply(List<LogEntry> logs) {
        List<Alert> alerts = new ArrayList<>();

        Map<String, List<LogEntry>> failedLogins = new HashMap<>();

        for (LogEntry log : logs) {
            if (log.getEventType().equalsIgnoreCase("ERROR") &&
                    log.getDescription().toLowerCase().contains("login")) {

                failedLogins
                        .computeIfAbsent(log.getIpAddress(), k -> new ArrayList<>())
                        .add(log);
            }
        }

        for (Map.Entry<String, List<LogEntry>> entry : failedLogins.entrySet()) {
            if (entry.getValue().size() >= THRESHOLD) {
                alerts.add(new Alert(
                        entry.getValue().get(0).getTimestamp(),
                        "HIGH",
                        "Multiple failed logins from IP: " + entry.getKey(),
                        entry.getValue()
                ));
            }
        }

        return alerts;
    }
}