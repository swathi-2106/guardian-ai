package com.example.logingestion.correlation;

import com.example.logingestion.LogEntry;
import com.example.logingestion.detection.Alert;
import com.example.logingestion.correlation.rules.*;

import java.util.ArrayList;
import java.util.List;

public class EventCorrelator {

    private final SequentialPatternRule sequentialRule = new SequentialPatternRule();
    private final RepeatedSuspiciousActivityRule repeatedRule = new RepeatedSuspiciousActivityRule();

    public List<CorrelatedIncident> correlate(List<LogEntry> logs, List<Alert> alerts) {

        List<CorrelatedIncident> incidents = new ArrayList<>();

        // Apply rules
        incidents.addAll(sequentialRule.apply(logs));
        incidents.addAll(repeatedRule.apply(logs));

        // Future: use alerts + ML models here

        return incidents;
    }
}