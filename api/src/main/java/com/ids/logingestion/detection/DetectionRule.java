package com.ids.logingestion.detection;

import com.ids.logingestion.LogEntry;
import java.util.List;

public interface DetectionRule {
    List<Alert> apply(List<LogEntry> logs);
}