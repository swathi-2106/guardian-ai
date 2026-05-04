package com.ids.logingestion;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogEntry {

    private String timestamp;
    private String ipAddress;
    private String eventType;
    private String description;
    private String dataMode;
    private String dataSource;

    public LogEntry() {
    }

    public LogEntry(String timestamp, String ipAddress, String eventType, String description) {
        this(timestamp, ipAddress, eventType, description, null, null);
    }

    public LogEntry(
            String timestamp,
            String ipAddress,
            String eventType,
            String description,
            String dataMode,
            String dataSource
    ) {
        this.timestamp = timestamp;
        this.ipAddress = ipAddress;
        this.eventType = eventType;
        this.description = description;
        this.dataMode = dataMode;
        this.dataSource = dataSource;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getEventType() {
        return eventType;
    }

    public String getDescription() {
        return description;
    }

    public String getDataMode() {
        return dataMode;
    }

    public String getDataSource() {
        return dataSource;
    }

    @JsonProperty("sourceType")
    public String getSourceType() {
        return dataSource;
    }

    public LogEntry withContext(String dataMode, String dataSource) {
        return new LogEntry(timestamp, ipAddress, eventType, description, dataMode, dataSource);
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "timestamp='" + timestamp + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", eventType='" + eventType + '\'' +
                ", description='" + description + '\'' +
                ", dataMode='" + dataMode + '\'' +
                ", dataSource='" + dataSource + '\'' +
                '}';
    }
}
