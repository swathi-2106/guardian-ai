package com.ids.logingestion.detection.rules;

import com.ids.logingestion.LogEntry;
import com.ids.logingestion.detection.Alert;
import com.ids.logingestion.detection.DetectionRule;

import java.util.*;

public class SuspiciousFileAccessRule implements DetectionRule {

    @Override
    public List<Alert> apply(List<LogEntry> logs) {
        List<Alert> alerts = new ArrayList<>();

        for (LogEntry log : logs) {
            String description = log.getDescription() == null ? "" : log.getDescription().toLowerCase();

            if (description.contains("failed") ||
                    description.contains("unauthorized") ||
                    description.contains("denied")) {

                alerts.add(new Alert(
                        log.getTimestamp(),
                        "HIGH",
                        "Suspicious activity detected: " + log.getDescription(),
                        List.of(log)
                ));
            }
        }

        return alerts;
    }
}
