package com.example.logingestion.detection;

import com.example.logingestion.LogEntry;
import com.example.logingestion.detection.rules.*;

import java.util.ArrayList;
import java.util.List;

public class IntrusionDetector {

    private final List<DetectionRule> rules = new ArrayList<>();

    public IntrusionDetector() {
        // Register rules here
        rules.add(new MultipleFailedLoginRule());
        rules.add(new UnknownIPRule());
        rules.add(new SuspiciousFileAccessRule());
    }

    public List<Alert> detect(List<LogEntry> logs) {
        List<Alert> alerts = new ArrayList<>();

        for (DetectionRule rule : rules) {
            alerts.addAll(rule.apply(logs));
        }

        return alerts;
    }
}