package com.ids.logingestion.query;

public class FilterCriteria {

    private String ip;
    private String eventType;
    private String startTime;
    private String endTime;
    private String keyword;

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}