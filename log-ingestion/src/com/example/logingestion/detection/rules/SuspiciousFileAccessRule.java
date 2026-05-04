package com.example.logingestion.detection.rules;

import com.example.logingestion.LogEntry;
import com.example.logingestion.detection.Alert;
import com.example.logingestion.detection.DetectionRule;

import java.util.*;

public class SuspiciousFileAccessRule implements DetectionRule {

    @Override
    public List<Alert> apply(List<LogEntry> logs) {
        List<Alert> alerts = new ArrayList<>();

        for (LogEntry log : logs) {
            if (log.getEventType().equalsIgnoreCase("FILE_ACCESS") &&
                    log.getDescription().toLowerCase().contains("sensitive")) {

                alerts.add(new Alert(
                        log.getTimestamp(),
                        "HIGH",
                        "Suspicious file access detected",
                        Collections.singletonList(log)
                ));
            }
        }

        return alerts;
    }
}