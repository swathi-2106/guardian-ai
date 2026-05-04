package com.ids.reporting;

import com.ids.logingestion.LogEntry;
import com.ids.logingestion.detection.Alert;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVExporter {

    // 🔹 Export Logs
    public void exportLogs(List<LogEntry> logs, String filePath) throws IOException {

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {

            // Header
            String[] header = {"Timestamp", "IP Address", "Event Type", "Description", "Data Mode", "Data Source"};
            writer.writeNext(header);

            // Data
            for (LogEntry log : logs) {
                String[] row = {
                        log.getTimestamp(),
                        log.getIpAddress(),
                        log.getEventType(),
                        log.getDescription(),
                        log.getDataMode(),
                        log.getDataSource()
                };
                writer.writeNext(row);
            }
        }
    }

    // 🔹 Export Alerts
    public void exportAlerts(List<Alert> alerts, String filePath) throws IOException {

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {

            // Header
            String[] header = {"Timestamp", "Severity", "Message", "Data Mode", "Data Source"};
            writer.writeNext(header);

            // Data
            for (Alert alert : alerts) {
                String[] row = {
                        alert.getTimestamp(),
                        alert.getSeverity(),
                        alert.getMessage(),
                        alert.getDataMode(),
                        alert.getDataSource()
                };
                writer.writeNext(row);
            }
        }
    }
}
