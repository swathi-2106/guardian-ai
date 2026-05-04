package com.example.logingestion.detection;

import com.example.logingestion.LogEntry;
import java.util.List;

public interface DetectionRule {
    List<Alert> apply(List<LogEntry> logs);
}