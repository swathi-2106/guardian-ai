package com.ids.logingestion;

import com.ids.logingestion.correlation.CorrelatedIncident;
import com.ids.logingestion.correlation.EventCorrelator;
import com.ids.logingestion.db.DatabaseHelper;
import com.ids.logingestion.detection.Alert;
import com.ids.logingestion.detection.IntrusionDetector;
import com.ids.logingestion.preprocessing.LogPreprocessor;
import com.ids.logingestion.query.FilterCriteria;
import com.ids.logingestion.query.LogFilter;
import com.ids.logingestion.timeline.TimelineBuilder;
import com.ids.logingestion.timeline.TimelineEvent;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Profile("cli")
public class Main implements CommandLineRunner {

    private final IntrusionDetector intrusionDetector;

    public Main(IntrusionDetector intrusionDetector) {
        this.intrusionDetector = intrusionDetector;
    }

    @Override
    public void run(String... args) {
        LogReader reader = new LogReader();
        LogPreprocessor preprocessor = new LogPreprocessor();

        DatabaseHelper db = new DatabaseHelper();
        db.initializeDatabase();

        try {
            List<LogEntry> rawLogs = reader.readLogs("src/main/java/com/ids/sample.log");

            System.out.println("=== RAW LOGS ===");
            rawLogs.forEach(System.out::println);

            List<LogEntry> cleanedLogs = preprocessor.preprocess(rawLogs);

            System.out.println("\n=== CLEANED LOGS ===");
            cleanedLogs.forEach(System.out::println);

            List<Alert> alerts = intrusionDetector.detect(cleanedLogs);

            System.out.println("\n=== ALERTS ===");
            alerts.forEach(System.out::println);

            EventCorrelator correlator = new EventCorrelator();
            List<CorrelatedIncident> incidents = correlator.correlate(cleanedLogs, alerts);

            System.out.println("\n=== CORRELATED INCIDENTS ===");
            incidents.forEach(System.out::println);

            TimelineBuilder timelineBuilder = new TimelineBuilder();
            List<TimelineEvent> timeline = timelineBuilder.buildFromLogs(cleanedLogs);

            System.out.println("\n=== TIMELINE ===");
            timeline.forEach(System.out::println);

            db.insertLogs(cleanedLogs);

            System.out.println("\n=== DB LOGS ===");
            db.fetchAllLogs().forEach(System.out::println);

            System.out.println("\n=== QUERY BY IP ===");
            db.getLogsByIP("192.168.1.3").forEach(System.out::println);

            FilterCriteria criteria = new FilterCriteria();
            criteria.setIp("192.168.1.3");

            LogFilter logFilter = new LogFilter();

            System.out.println("\n=== FILTERED LOGS ===");
            logFilter.filter(cleanedLogs, criteria).forEach(System.out::println);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
