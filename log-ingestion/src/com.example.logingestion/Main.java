//package com.example.logingestion;
//
//import java.io.IOException;
//import java.util.List;
//
//public class Main {
//
//    public static void main(String[] args) {
//        LogReader reader = new LogReader();
//
//        try {
//            List<LogEntry> logs = reader.readLogs("sample.log");
//
//            for (LogEntry log : logs) {
//                System.out.println(log);
//            }
//
//        } catch (IOException e) {
//            System.err.println("Error reading file: " + e.getMessage());
//        }
//    }
//}
package com.example.logingestion;

import com.example.logingestion.preprocessing.LogPreprocessor;
import com.example.logingestion.detection.IntrusionDetector;
import com.example.logingestion.detection.Alert;
import com.example.logingestion.correlation.EventCorrelator;
import com.example.logingestion.correlation.CorrelatedIncident;
import com.example.logingestion.timeline.TimelineBuilder;
import com.example.logingestion.timeline.TimelineEvent;
import com.example.logingestion.db.DatabaseHelper;
import com.example.logingestion.query.*;
import com.example.logingestion.db.DatabaseHelper;
import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        LogReader reader = new LogReader();
        LogPreprocessor preprocessor = new LogPreprocessor();

        DatabaseHelper db = new DatabaseHelper();
        db.initializeDatabase();



        try {
            List<LogEntry> rawLogs = reader.readLogs("sample.log");

            System.out.println("=== RAW LOGS ===");
            rawLogs.forEach(System.out::println);

            List<LogEntry> cleanedLogs = preprocessor.preprocess(rawLogs);

            System.out.println("\n=== CLEANED LOGS ===");
            cleanedLogs.forEach(System.out::println);

            IntrusionDetector detector = new IntrusionDetector();

            List<Alert> alerts = detector.detect(cleanedLogs);

            System.out.println("\n=== ALERTS ===");
            alerts.forEach(System.out::println);

            EventCorrelator correlator = new EventCorrelator();

            List<CorrelatedIncident> incidents =
                    correlator.correlate(cleanedLogs, alerts);

            System.out.println("\n=== CORRELATED INCIDENTS ===");
            incidents.forEach(System.out::println);

            TimelineBuilder timelineBuilder = new TimelineBuilder();

// Option 1: From logs
            List<TimelineEvent> timeline = timelineBuilder.buildFromLogs(cleanedLogs);

// Option 2 (better): From incidents
// List<TimelineEvent> timeline = timelineBuilder.buildFromIncidents(incidents);

            System.out.println("\n=== TIMELINE ===");
            timeline.forEach(System.out::println);


//            DatabaseHelper db = new DatabaseHelper();

// Insert logs
            db.insertLogs(cleanedLogs);

// Fetch all
            System.out.println("\n=== DB LOGS ===");
            db.fetchAllLogs().forEach(System.out::println);

// Query by IP
            System.out.println("\n=== QUERY BY IP ===");
            db.getLogsByIP("192.168.1.3").forEach(System.out::println);


            FilterCriteria criteria = new FilterCriteria();
            criteria.setIp("192.168.1.3");

            LogFilter logFilter = new LogFilter();

            System.out.println("\n=== FILTERED LOGS ===");
            logFilter.filter(cleanedLogs, criteria)
                    .forEach(System.out::println);

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

    }
}