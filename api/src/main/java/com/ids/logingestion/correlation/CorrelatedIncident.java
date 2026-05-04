package com.ids.logingestion.correlation;

import com.ids.logingestion.LogEntry;
import java.util.List;

public class CorrelatedIncident {

    private List<LogEntry> relatedEvents;
    private String attackType;
    private double confidence;

    public CorrelatedIncident(List<LogEntry> relatedEvents, String attackType, double confidence) {
        this.relatedEvents = relatedEvents;
        this.attackType = attackType;
        this.confidence = confidence;
    }

    public List<LogEntry> getRelatedEvents() {
        return relatedEvents;
    }

    public String getAttackType() {
        return attackType;
    }

    public double getConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        return "CorrelatedIncident{" +
                "attackType='" + attackType + '\'' +
                ", confidence=" + confidence +
                ", relatedEvents=" + relatedEvents +
                '}';
    }
}