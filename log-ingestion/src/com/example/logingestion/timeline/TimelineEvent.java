package com.example.logingestion.timeline;

import com.example.logingestion.LogEntry;

public class TimelineEvent {

    private String timestamp;
    private String ipAddress;
    private String eventType;
    private String description;
    private boolean suspicious;

    public TimelineEvent(String timestamp, String ipAddress,
                         String eventType, String description,
                         boolean suspicious) {
        this.timestamp = timestamp;
        this.ipAddress = ipAddress;
        this.eventType = eventType;
        this.description = description;
        this.suspicious = suspicious;
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

    public boolean isSuspicious() {
        return suspicious;
    }

    @Override
    public String toString() {
        return "TimelineEvent{" +
                "timestamp='" + timestamp + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", eventType='" + eventType + '\'' +
                ", description='" + description + '\'' +
                ", suspicious=" + suspicious +
                '}';
    }

    public static TimelineEvent fromLog(LogEntry log, boolean suspicious) {
        return new TimelineEvent(
                log.getTimestamp(),
                log.getIpAddress(),
                log.getEventType(),
                log.getDescription(),
                suspicious
        );
    }
}