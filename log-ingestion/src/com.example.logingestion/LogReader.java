package com.example.logingestion;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LogReader {

    public List<LogEntry> readLogs(String filePath) throws IOException {
        List<LogEntry> logEntries = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {

                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    LogEntry entry = parseLine(line);
                    if (entry != null) {
                        logEntries.add(entry);
                    }
                } catch (IllegalArgumentException e) {
                    // Handle invalid log format (skip but continue processing)
                    System.err.println("Skipping invalid log: " + line);
                }
            }
        }

        return logEntries;
    }

    private LogEntry parseLine(String line) {
        // Regex to match: [timestamp] [ip] [event] [description]
        String regex = "^\\[(.*?)\\] \\[(.*?)\\] \\[(.*?)\\] \\[(.*?)\\]$";

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(line);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid log format");
        }

        String timestamp = matcher.group(1);
        String ipAddress = matcher.group(2);
        String eventType = matcher.group(3);
        String description = matcher.group(4);

        return new LogEntry(timestamp, ipAddress, eventType, description);
    }
}