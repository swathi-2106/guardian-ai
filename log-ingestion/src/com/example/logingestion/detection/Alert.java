package com.example.logingestion.detection;

import com.example.logingestion.LogEntry;
import java.util.List;

public class Alert {

    private String timestamp;
    private String severity;
    private String message;
    private List<LogEntry> relatedLogs;

    public Alert(String timestamp, String severity, String message, List<LogEntry> relatedLogs) {
        this.timestamp = timestamp;
        this.severity = severity;
        this.message = message;
        this.relatedLogs = relatedLogs;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public List<LogEntry> getRelatedLogs() {
        return relatedLogs;
    }

    @Override
    public String toString() {
        return "Alert{" +
                "timestamp='" + timestamp + '\'' +
                ", severity='" + severity + '\'' +
                ", message='" + message + '\'' +
                ", relatedLogs=" + relatedLogs +
                '}';
    }
}