package com.ids.logingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogReader {

    private static final Logger logger = LoggerFactory.getLogger(LogReader.class);
    private static final Pattern BRACKETED_LOG_PATTERN = Pattern.compile(
            "^\\[(.+?)]\\s+\\[(.+?)]\\s+\\[(.+?)]\\s+\\[(.+)]$"
    );
    private static final Pattern IP_PATTERN = Pattern.compile("(\\d{1,3}(\\.\\d{1,3}){3})");

    public List<LogEntry> readLogs(String filePath) throws IOException {
        List<LogEntry> logEntries = new ArrayList<>();

        BufferedReader reader;

        if (filePath.contains("system_logs")) {
            reader = Files.newBufferedReader(
                    Path.of(filePath),
                    java.nio.charset.StandardCharsets.UTF_16
            );
        } else {
            reader = Files.newBufferedReader(
                    Path.of(filePath),
                    java.nio.charset.StandardCharsets.UTF_8
            );
        }

        try (reader) {
            String line;
            int parsedCount = 0;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                if (!line.contains("[") || !line.contains("]")) {
                    continue;
                }

                try {
                    LogEntry entry = parseLine(line);
                    if (entry != null) {
                        logEntries.add(entry);
                        parsedCount++;
                    }
                } catch (Exception e) {
                    logger.warn("Skipping invalid log line: {}", line);
                }
            }

            logger.info("Parsed {} log entries from {}", parsedCount, filePath);
        }

        return logEntries;
    }

    public LogEntry parseLine(String line) {
        try {
            Matcher bracketedMatcher = BRACKETED_LOG_PATTERN.matcher(line.trim());
            if (bracketedMatcher.matches()) {
                LogEntry entry = new LogEntry(
                        bracketedMatcher.group(1).trim(),
                        bracketedMatcher.group(2).trim(),
                        bracketedMatcher.group(3).trim(),
                        bracketedMatcher.group(4).trim()
                );
                logger.debug("Parsed bracketed log entry type={} ip={}", entry.getEventType(), entry.getIpAddress());
                return entry;
            }

            int firstBracket = line.indexOf("[");
            int secondBracket = line.indexOf("]");

            if (firstBracket == -1 || secondBracket == -1) {
                return null;
            }

            String timestamp = line.substring(0, firstBracket).trim();
            String eventType = line.substring(firstBracket + 1, secondBracket).trim();
            String description = line.substring(secondBracket + 1).trim();

            String ip = "LOCAL";
            Matcher matcher = IP_PATTERN.matcher(description);
            if (matcher.find()) {
                ip = matcher.group();
            }

            LogEntry entry = new LogEntry(timestamp, ip, eventType, description);
            logger.debug("Parsed standard log entry type={} ip={}", entry.getEventType(), entry.getIpAddress());
            return entry;
        } catch (Exception e) {
            logger.warn("Failed to parse log line: {}", line, e);
            return null;
        }
    }
}
