package com.ids.logingestion.query;

import com.ids.logingestion.LogEntry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class LogFilter {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<LogEntry> filter(List<LogEntry> logs, FilterCriteria criteria) {

        return logs.stream()
                .filter(log -> criteria.getIp() == null ||
                        log.getIpAddress().equals(criteria.getIp()))

                .filter(log -> criteria.getEventType() == null ||
                        log.getEventType().equalsIgnoreCase(criteria.getEventType()))

                .filter(log -> {
                    if (criteria.getStartTime() == null || criteria.getEndTime() == null)
                        return true;

                    LocalDateTime logTime = LocalDateTime.parse(log.getTimestamp(), FORMATTER);
                    LocalDateTime start = LocalDateTime.parse(criteria.getStartTime(), FORMATTER);
                    LocalDateTime end = LocalDateTime.parse(criteria.getEndTime(), FORMATTER);

                    return !logTime.isBefore(start) && !logTime.isAfter(end);
                })

                .filter(log -> criteria.getKeyword() == null ||
                        log.getDescription().toLowerCase()
                                .contains(criteria.getKeyword().toLowerCase()))

                .collect(Collectors.toList());
    }
}