package com.example.logingestion;

public class LogEntry {

    private String timestamp;
    private String ipAddress;
    private String eventType;
    private String description;

    public LogEntry(String timestamp, String ipAddress, String eventType, String description) {
        this.timestamp = timestamp;
        this.ipAddress = ipAddress;
        this.eventType = eventType;
        this.description = description;
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

    @Override
    public String toString() {
        return "LogEntry{" +
                "timestamp='" + timestamp + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", eventType='" + eventType + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}