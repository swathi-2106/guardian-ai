package com.ids.logingestion.preprocessing;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class LogUtils {

    // Standard output format
    private static final DateTimeFormatter OUTPUT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Example input format (can extend later)
    private static final DateTimeFormatter INPUT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Simple IP regex
    private static final Pattern IP_PATTERN =
            Pattern.compile("^(?:\\d{1,3}\\.){3}\\d{1,3}$");

    public static String normalizeTimestamp(String timestamp) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(timestamp, INPUT_FORMAT);
            return dateTime.format(OUTPUT_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid timestamp: " + timestamp);
        }
    }

    public static boolean isValidIP(String ip) {
        return IP_PATTERN.matcher(ip).matches();
    }

    public static String categorizeEvent(String eventType) {
        if (eventType == null) return "UNKNOWN";

        eventType = eventType.toUpperCase();

        if (eventType.contains("LOGIN") || eventType.contains("LOGOUT")) {
            return "AUTH";
        } else if (eventType.contains("FILE")) {
            return "FILE_ACCESS";
        } else if (eventType.contains("NET") || eventType.contains("IP")) {
            return "NETWORK";
        } else if (eventType.contains("ERROR") || eventType.contains("FAIL")) {
            return "ERROR";
        }

        return "OTHER";
    }
}