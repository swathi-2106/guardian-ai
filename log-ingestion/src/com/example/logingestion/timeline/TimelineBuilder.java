package com.example.logingestion.timeline;

import com.example.logingestion.LogEntry;
import com.example.logingestion.correlation.CorrelatedIncident;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TimelineBuilder {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<TimelineEvent> buildFromLogs(List<LogEntry> logs) {

        TreeMap<LocalDateTime, List<TimelineEvent>> timelineMap = new TreeMap<>();

        for (LogEntry log : logs) {

            LocalDateTime time = LocalDateTime.parse(log.getTimestamp(), FORMATTER);

            boolean suspicious = log.getEventType().equalsIgnoreCase("ERROR");

            TimelineEvent event = TimelineEvent.fromLog(log, suspicious);

            timelineMap
                    .computeIfAbsent(time, k -> new ArrayList<>())
                    .add(event);
        }

        return flattenTimeline(timelineMap);
    }

    public List<TimelineEvent> buildFromIncidents(List<CorrelatedIncident> incidents) {

        TreeMap<LocalDateTime, List<TimelineEvent>> timelineMap = new TreeMap<>();

        for (CorrelatedIncident incident : incidents) {

            for (LogEntry log : incident.getRelatedEvents()) {

                LocalDateTime time = LocalDateTime.parse(log.getTimestamp(), FORMATTER);

                TimelineEvent event = new TimelineEvent(
                        log.getTimestamp(),
                        log.getIpAddress(),
                        incident.getAttackType(), // override type with attack label
                        log.getDescription(),
                        true // incidents are suspicious
                );

                timelineMap
                        .computeIfAbsent(time, k -> new ArrayList<>())
                        .add(event);
            }
        }

        return flattenTimeline(timelineMap);
    }

    private List<TimelineEvent> flattenTimeline(
            TreeMap<LocalDateTime, List<TimelineEvent>> timelineMap) {

        List<TimelineEvent> result = new ArrayList<>();

        for (Map.Entry<LocalDateTime, List<TimelineEvent>> entry : timelineMap.entrySet()) {
            result.addAll(entry.getValue());
        }

        return result;
    }
}