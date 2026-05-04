package com.example.logingestion.query;

import com.example.logingestion.detection.Alert;

import java.util.List;
import java.util.stream.Collectors;

public class AlertFilter {

    public List<Alert> filter(List<Alert> alerts, FilterCriteria criteria) {

        return alerts.stream()
                .filter(alert -> criteria.getKeyword() == null ||
                        alert.getMessage().toLowerCase()
                                .contains(criteria.getKeyword().toLowerCase()))

                .filter(alert -> criteria.getStartTime() == null ||
                        alert.getTimestamp().compareTo(criteria.getStartTime()) >= 0)

                .filter(alert -> criteria.getEndTime() == null ||
                        alert.getTimestamp().compareTo(criteria.getEndTime()) <= 0)

                .collect(Collectors.toList());
    }
}