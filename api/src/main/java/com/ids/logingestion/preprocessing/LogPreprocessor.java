package com.ids.logingestion.preprocessing;

import com.ids.logingestion.LogEntry;

import java.util.ArrayList;
import java.util.List;

public class LogPreprocessor {

    public List<LogEntry> preprocess(List<LogEntry> logs) {
        List<LogEntry> cleanedLogs = new ArrayList<>();

        for (LogEntry log : logs) {
            try {
                String normalizedTimestamp =
                        LogUtils.normalizeTimestamp(log.getTimestamp());

                if (!LogUtils.isValidIP(log.getIpAddress())) {
                    throw new IllegalArgumentException("Invalid IP: " + log.getIpAddress());
                }

                String category =
                        LogUtils.categorizeEvent(log.getEventType());

                LogEntry cleaned = new LogEntry(
                        normalizedTimestamp,
                        log.getIpAddress(),
                        category,
                        log.getDescription()
                );

                cleanedLogs.add(cleaned);

            } catch (Exception e) {
                // Skip malformed entries but continue processing
                System.err.println("Skipping malformed log: " + log);
            }
        }

        return cleanedLogs;
    }
}