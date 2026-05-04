package com.example.logingestion.correlation.rules;

import com.example.logingestion.LogEntry;
import com.example.logingestion.correlation.CorrelatedIncident;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SequentialPatternRule {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<CorrelatedIncident> apply(List<LogEntry> logs) {
        List<CorrelatedIncident> incidents = new ArrayList<>();

        Map<String, List<LogEntry>> groupedByIP = new HashMap<>();

        for (LogEntry log : logs) {
            groupedByIP
                    .computeIfAbsent(log.getIpAddress(), k -> new ArrayList<>())
                    .add(log);
        }

        for (List<LogEntry> ipLogs : groupedByIP.values()) {

            ipLogs.sort(Comparator.comparing(log ->
                    LocalDateTime.parse(log.getTimestamp(), FORMATTER)));

            for (int i = 0; i < ipLogs.size() - 2; i++) {

                LogEntry first = ipLogs.get(i);
                LogEntry second = ipLogs.get(i + 1);
                LogEntry third = ipLogs.get(i + 2);

                if (first.getEventType().equals("AUTH") &&
                        second.getEventType().equals("FILE_ACCESS") &&
                        third.getEventType().equals("NETWORK")) {

                    List<LogEntry> pattern = Arrays.asList(first, second, third);

                    incidents.add(new CorrelatedIncident(
                            pattern,
                            "Potential Data Exfiltration",
                            0.9
                    ));
                }
            }
        }

        return incidents;
    }
}