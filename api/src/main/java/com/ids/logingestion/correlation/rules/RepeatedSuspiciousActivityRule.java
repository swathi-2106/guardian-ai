package com.ids.logingestion.correlation.rules;

import com.ids.logingestion.LogEntry;
import com.ids.logingestion.correlation.CorrelatedIncident;

import java.util.*;

public class RepeatedSuspiciousActivityRule {

    private static final int THRESHOLD = 3;

    public List<CorrelatedIncident> apply(List<LogEntry> logs) {
        List<CorrelatedIncident> incidents = new ArrayList<>();

        Map<String, List<LogEntry>> suspiciousMap = new HashMap<>();

        for (LogEntry log : logs) {
            if (log.getEventType().equals("ERROR")) {
                suspiciousMap
                        .computeIfAbsent(log.getIpAddress(), k -> new ArrayList<>())
                        .add(log);
            }
        }

        for (Map.Entry<String, List<LogEntry>> entry : suspiciousMap.entrySet()) {
            if (entry.getValue().size() >= THRESHOLD) {

                incidents.add(new CorrelatedIncident(
                        entry.getValue(),
                        "Repeated Suspicious Activity",
                        0.8
                ));
            }
        }

        return incidents;
    }
}