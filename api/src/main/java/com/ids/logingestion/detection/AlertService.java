package com.ids.logingestion.detection;

import com.ids.logingestion.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AlertService {

    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);
    private static final Set<String> SYSTEM_NOISE_KEYWORDS = Set.of(
            "firmware", "hardware", "pluton", "driver"
    );
    private static final Set<String> SENSITIVE_FILE_KEYWORDS = Set.of(
            "passwd", "shadow", "credential", "secret", "key", "wallet",
            "config", ".pem", ".pfx", ".jks", ".kdb", ".env", "id_rsa"
    );
    private static final Set<String> UNUSUAL_USER_KEYWORDS = Set.of(
            "guest", "anonymous", "unknown user", "temp", "test", "nobody"
    );
    private static final List<DateTimeFormatter> TIMESTAMP_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
    );
    private static final Pattern USER_PATTERN = Pattern.compile(
            "(?i)(?:user|account|subject|principal)\\s*[:=]?\\s*([a-z0-9._\\\\-]+)"
    );
    private static final long LOGIN_WINDOW_MINUTES = 5;
    private static final int FAILED_LOGIN_THRESHOLD = 4;
    private static final long FILE_ACCESS_WINDOW_MINUTES = 5;
    private static final int REPEATED_FILE_ACCESS_THRESHOLD = 3;
    private static final int REPEATED_AI_ANOMALY_THRESHOLD = 2;

    public List<Alert> generateAlerts(
            String mode,
            String source,
            List<LogEntry> logs,
            List<LogEntry> aiAnomalies
    ) {
        if (logs == null || logs.isEmpty()) {
            return List.of();
        }

        List<LogEntry> candidateLogs = logs.stream()
                .filter(log -> !isSystemNoise(log))
                .sorted(Comparator.comparing(this::timestampOrMin))
                .toList();

        Map<String, Alert> dedupedAlerts = new LinkedHashMap<>();
        addAlerts(dedupedAlerts, buildFailedLoginAlerts(candidateLogs, mode, source));
        addAlerts(dedupedAlerts, buildSuspiciousFileAccessAlerts(candidateLogs, mode, source));
        addAlerts(dedupedAlerts, buildRepeatedAiAnomalyAlerts(candidateLogs, aiAnomalies, mode, source));

        logger.info(
                "AlertService reduced {} logs to {} candidate logs and produced {} alerts",
                logs.size(),
                candidateLogs.size(),
                dedupedAlerts.size()
        );
        return new ArrayList<>(dedupedAlerts.values());
    }

    private void addAlerts(Map<String, Alert> target, List<Alert> alerts) {
        for (Alert alert : alerts) {
            target.put(buildKey(alert), alert);
        }
    }

    private List<Alert> buildFailedLoginAlerts(List<LogEntry> logs, String mode, String source) {
        Map<String, List<LogEntry>> failedLoginGroups = logs.stream()
                .filter(this::isFailedLogin)
                .collect(Collectors.groupingBy(this::normalizedIp));

        List<Alert> alerts = new ArrayList<>();
        for (Map.Entry<String, List<LogEntry>> entry : failedLoginGroups.entrySet()) {
            List<List<LogEntry>> windows = buildTimeWindows(entry.getValue(), LOGIN_WINDOW_MINUTES);
            for (List<LogEntry> window : windows) {
                if (window.size() >= FAILED_LOGIN_THRESHOLD) {
                    String severity;
                    if (window.size() >= 8) {
                        severity = "CRITICAL";
                    } else if (window.size() >= 5) {
                        severity = "HIGH";
                    } else if (window.size() >= 3) {
                        severity = "MEDIUM";
                    } else {
                        severity = "LOW";
                    }
                    alerts.add(new Alert(
                            window.get(window.size() - 1).getTimestamp(),
                            severity,
                            "Repeated failed login attempts from IP: " + entry.getKey(),
                            window,
                            mode,
                            source
                    ));
                }
            }
        }
        return alerts;
    }

    private List<Alert> buildSuspiciousFileAccessAlerts(List<LogEntry> logs, String mode, String source) {
        List<LogEntry> fileAccessLogs = logs.stream()
                .filter(this::isFileAccessEvent)
                .toList();

        Map<String, List<LogEntry>> groupedByActorAndTarget = fileAccessLogs.stream()
                .collect(Collectors.groupingBy(log -> normalizedIp(log) + "|" + extractSensitiveTarget(log)));

        List<Alert> alerts = new ArrayList<>();
        for (Map.Entry<String, List<LogEntry>> entry : groupedByActorAndTarget.entrySet()) {
            List<LogEntry> group = entry.getValue();
            boolean sensitiveTarget = group.stream().anyMatch(this::hasSensitiveFileIndicator);
            boolean unusualUser = group.stream().anyMatch(this::hasUnusualUserIndicator);

            if (sensitiveTarget && unusualUser) {
                LogEntry latest = group.get(group.size() - 1);
                alerts.add(new Alert(
                        latest.getTimestamp(),
                        "HIGH",
                        "Sensitive file access from unusual user context",
                        group,
                        mode,
                        source
                ));
                continue;
            }

            List<List<LogEntry>> windows = buildTimeWindows(group, FILE_ACCESS_WINDOW_MINUTES);
            for (List<LogEntry> window : windows) {

                LogEntry latest = window.get(window.size() - 1); // ✅ FIX

                String severity;
                if (window.size() >= 6) {
                    severity = "CRITICAL";
                } else if (window.size() >= 3) {
                    severity = sensitiveTarget ? "HIGH" : "MEDIUM";
                } else {
                    severity = "LOW";
                }

                alerts.add(new Alert(
                        latest.getTimestamp(),
                        severity,
                        "Suspicious file access activity detected",
                        window,
                        mode,
                        source
                ));
            }
        }
        return alerts;
    }

    private List<Alert> buildRepeatedAiAnomalyAlerts(
            List<LogEntry> candidateLogs,
            List<LogEntry> aiAnomalies,
            String mode,
            String source
    ) {
        if (aiAnomalies == null || aiAnomalies.isEmpty()) {
            return List.of();
        }

        Map<String, LogEntry> knownLogIndex = candidateLogs.stream()
                .collect(Collectors.toMap(this::signature, Function.identity(), (left, right) -> left));

        List<LogEntry> filteredAnomalies = aiAnomalies.stream()
                .map(anomaly -> knownLogIndex.getOrDefault(signature(anomaly), anomaly))
                .filter(log -> !isSystemNoise(log))
                .sorted(Comparator.comparing(this::timestampOrMin))
                .toList();

        Map<String, List<LogEntry>> groupedAnomalies = filteredAnomalies.stream()
                .collect(Collectors.groupingBy(log -> normalizedIp(log) + "|" + normalizedEvent(log)));

        List<Alert> alerts = new ArrayList<>();
        for (List<LogEntry> group : groupedAnomalies.values()) {
            List<List<LogEntry>> windows = buildTimeWindows(group, FILE_ACCESS_WINDOW_MINUTES);
            for (List<LogEntry> window : windows) {
                if (window.size() >= REPEATED_AI_ANOMALY_THRESHOLD) {
                    LogEntry latest = window.get(window.size() - 1);
                    alerts.add(new Alert(
                            latest.getTimestamp(),
                            "MEDIUM",
                            "Repeated anomalous activity detected",
                            window,
                            mode,
                            source
                    ));
                }
            }
        }

        return alerts;
    }

    private boolean isSystemNoise(LogEntry log) {
        String combinedText = (normalizedEvent(log) + " " + normalizedDescription(log)).toLowerCase(Locale.ROOT);
        return SYSTEM_NOISE_KEYWORDS.stream().anyMatch(combinedText::contains);
    }

    private boolean isFailedLogin(LogEntry log) {
        String eventType = normalizedEvent(log);
        String description = normalizedDescription(log);
        boolean loginContext = eventType.contains("auth")
                || description.contains("login")
                || description.contains("log on")
                || description.contains("sign in");
        boolean failureContext = description.contains("fail")
                || description.contains("4625")
                || description.contains("denied")
                || description.contains("invalid password")
                || description.contains("bad password");
        return loginContext && failureContext;
    }

    private boolean isFileAccessEvent(LogEntry log) {
        String eventType = normalizedEvent(log);
        String description = normalizedDescription(log);
        return eventType.contains("file")
                || description.contains("file")
                || description.contains("path")
                || description.contains("directory");
    }

    private boolean hasSensitiveFileIndicator(LogEntry log) {
        String description = normalizedDescription(log);
        return SENSITIVE_FILE_KEYWORDS.stream().anyMatch(description::contains);
    }

    private boolean hasUnusualUserIndicator(LogEntry log) {
        String description = normalizedDescription(log);
        if (UNUSUAL_USER_KEYWORDS.stream().anyMatch(description::contains)) {
            return true;
        }

        Matcher matcher = USER_PATTERN.matcher(description);
        if (!matcher.find()) {
            return false;
        }

        String user = matcher.group(1).toLowerCase(Locale.ROOT);
        return user.startsWith("guest")
                || user.startsWith("temp")
                || user.startsWith("test")
                || user.contains("anonymous");
    }

    private String extractSensitiveTarget(LogEntry log) {
        if (hasSensitiveFileIndicator(log)) {
            return "sensitive";
        }
        return normalizedEvent(log);
    }

    private List<List<LogEntry>> buildTimeWindows(List<LogEntry> logs, long minutes) {
        List<List<LogEntry>> windows = new ArrayList<>();
        if (logs.isEmpty()) {
            return windows;
        }

        int start = 0;
        for (int end = 0; end < logs.size(); end++) {
            while (start < end && exceedsWindow(logs.get(start), logs.get(end), minutes)) {
                start++;
            }

            List<LogEntry> window = new ArrayList<>(logs.subList(start, end + 1));
            windows.add(window);
        }
        return windows;
    }

    private boolean exceedsWindow(LogEntry start, LogEntry end, long minutes) {
        LocalDateTime startTime = timestampOrMin(start);
        LocalDateTime endTime = timestampOrMin(end);
        return Duration.between(startTime, endTime).toMinutes() > minutes;
    }

    private LocalDateTime timestampOrMin(LogEntry log) {
        if (log == null || log.getTimestamp() == null || log.getTimestamp().isBlank()) {
            return LocalDateTime.MIN;
        }

        String rawTimestamp = log.getTimestamp().trim();
        for (DateTimeFormatter formatter : TIMESTAMP_FORMATTERS) {
            try {
                return LocalDateTime.parse(rawTimestamp, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        return LocalDateTime.MIN;
    }

    private String signature(LogEntry log) {
        return String.join("|",
                String.valueOf(log.getTimestamp()),
                normalizedIp(log),
                normalizedEvent(log),
                normalizedDescription(log));
    }

    private String normalizedIp(LogEntry log) {
        String ipAddress = log.getIpAddress();
        if (ipAddress == null || ipAddress.isBlank()) {
            return "LOCAL";
        }
        return ipAddress.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizedEvent(LogEntry log) {
        return log.getEventType() == null
                ? ""
                : log.getEventType().trim().toLowerCase(Locale.ROOT);
    }

    private String normalizedDescription(LogEntry log) {
        return log.getDescription() == null
                ? ""
                : log.getDescription().trim().toLowerCase(Locale.ROOT);
    }

    private String buildKey(Alert alert) {
        return String.join("|",
                String.valueOf(alert.getTimestamp()),
                String.valueOf(alert.getSeverity()),
                String.valueOf(alert.getMessage())
        );
    }
}
