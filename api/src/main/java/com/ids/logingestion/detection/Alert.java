package com.ids.logingestion.detection;

import com.ids.logingestion.LogEntry;
import java.util.List;

public class Alert {

    private String timestamp;
    private String severity;
    private String message;
    private List<LogEntry> relatedLogs;
    private String dataMode;
    private String dataSource;

    public Alert(String timestamp, String severity, String message, List<LogEntry> relatedLogs) {
        this(timestamp, severity, message, relatedLogs, null, null);
    }

    public Alert(
            String timestamp,
            String severity,
            String message,
            List<LogEntry> relatedLogs,
            String dataMode,
            String dataSource
    ) {
        this.timestamp = timestamp;
        this.severity = severity;
        this.message = message;
        this.relatedLogs = relatedLogs;
        this.dataMode = dataMode;
        this.dataSource = dataSource;
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

    public String getDataMode() {
        return dataMode;
    }

    public String getDataSource() {
        return dataSource;
    }

    public Alert withContext(String dataMode, String dataSource) {
        return new Alert(timestamp, severity, message, relatedLogs, dataMode, dataSource);
    }

    @Override
    public String toString() {
        return "Alert{" +
                "timestamp='" + timestamp + '\'' +
                ", severity='" + severity + '\'' +
                ", message='" + message + '\'' +
                ", relatedLogs=" + relatedLogs +
                ", dataMode='" + dataMode + '\'' +
                ", dataSource='" + dataSource + '\'' +
                '}';
    }
}
