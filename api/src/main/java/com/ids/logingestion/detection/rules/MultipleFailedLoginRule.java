package com.ids.logingestion.detection.rules;

import com.ids.logingestion.LogEntry;
import com.ids.logingestion.detection.Alert;
import com.ids.logingestion.detection.DetectionRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultipleFailedLoginRule implements DetectionRule {

    private static final int THRESHOLD = 3;

    @Override
    public List<Alert> apply(List<LogEntry> logs) {
        List<Alert> alerts = new ArrayList<>();
        Map<String, List<LogEntry>> failedLogins = new HashMap<>();

        for (LogEntry log : logs) {
            String eventType = log.getEventType() == null ? "" : log.getEventType().toLowerCase();
            String description = log.getDescription() == null ? "" : log.getDescription().toLowerCase();

            boolean failedLogin = (description.contains("login") || description.contains("log on"))
                    && (description.contains("fail") || description.contains("4625") || description.contains("denied"));

            if (failedLogin || eventType.contains("auth")) {
                String ipAddress = log.getIpAddress() == null || log.getIpAddress().isBlank()
                        ? "LOCAL"
                        : log.getIpAddress();

                failedLogins.computeIfAbsent(ipAddress, key -> new ArrayList<>()).add(log);
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
