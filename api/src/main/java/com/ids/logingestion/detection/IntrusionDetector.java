package com.ids.logingestion.detection;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ids.logingestion.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

@Service
public class IntrusionDetector {

    private static final Logger logger = LoggerFactory.getLogger(IntrusionDetector.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final AlertEmailService alertEmailService;
    private final AlertService alertService;

//    public IntrusionDetector() {
//        this(null);
//    }

    @Autowired
    public IntrusionDetector(AlertEmailService alertEmailService, AlertService alertService) {
        this.alertEmailService = alertEmailService;
        this.alertService = alertService;
    }

    public List<Alert> detect(List<LogEntry> logs) {
        return detect("REAL", "UNSPECIFIED", logs);
    }

    public List<Alert> detect(String mode, String source, List<LogEntry> logs) {
        logger.info("Starting intrusion detection for {} logs", logs == null ? 0 : logs.size());
        logger.info("Detection context mode={} source={}", mode, source);

        List<LogEntry> aiAnomalies = detectAnomaliesWithAI(logs);
        logger.info("AI stage returned {} anomalous logs", aiAnomalies.size());

        List<Alert> alerts = alertService.generateAlerts(mode, source, logs, aiAnomalies);
        logger.info("Detection pipeline produced {} total alerts", alerts.size());
        boolean emailAttempted = notifyNewAlerts(alerts, mode, source);
        logger.info(
                "Detection summary mode={} source={} fetchedLogs={} generatedAlerts={} emailAttempted={}",
                mode,
                source,
                logs == null ? 0 : logs.size(),
                alerts.size(),
                emailAttempted
        );
        return alerts;
    }

    private List<LogEntry> detectAnomaliesWithAI(List<LogEntry> logs) {
        List<LogEntry> anomalies = new ArrayList<>();
        if (logs == null || logs.isEmpty()) {
            return anomalies;
        }

        try {
            String pythonCommand = resolvePythonCommand();
            if (pythonCommand == null) {
                logger.info("Skipping AI anomaly detection because Python is not available on PATH");
                return anomalies;
            }

            ProcessBuilder pb = new ProcessBuilder(pythonCommand, "ai_detector.py");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                writer.write(mapper.writeValueAsString(logs));
            }

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            process.waitFor();

            if (!output.isEmpty()) {
                anomalies.addAll(mapper.readValue(
                        output.toString(),
                        new TypeReference<List<LogEntry>>() {}
                ));
            }
        } catch (Exception e) {
            logger.warn("AI anomaly detection failed, continuing with rule-based detection only", e);
        }

        return anomalies;
    }

    private String resolvePythonCommand() {
        for (String candidate : List.of("python", "py")) {
            try {
                Process process = new ProcessBuilder(candidate, "--version")
                        .redirectErrorStream(true)
                        .start();
                if (process.waitFor() == 0) {
                    return candidate;
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private boolean notifyNewAlerts(List<Alert> alerts, String mode, String source) {
        if (alertEmailService == null) {
            logger.warn("AlertEmailService not configured for IntrusionDetector. mode={} source={}", mode, source);
            return false;
        }

        boolean attempted = false;
        for (Alert alert : alerts) {
            attempted = true;
            logger.info("Attempting email notification for alert mode={} source={} alert={}", mode, source, alert);
            alertEmailService.notifyIfNew(alert, mode, source);
        }
        return attempted;
    }
}
