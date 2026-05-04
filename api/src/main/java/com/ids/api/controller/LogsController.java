package com.ids.api.controller;

import com.ids.logingestion.LogEntry;
import com.ids.logingestion.LogReader;
import com.ids.logingestion.db.DatabaseHelper;
import com.ids.logingestion.detection.Alert;
import com.ids.logingestion.detection.IntrusionDetector;
import com.ids.reporting.CSVExporter;
import com.ids.reporting.PDFGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
public class LogsController {

    private static final Logger logger = LoggerFactory.getLogger(LogsController.class);
    private static final String MODE_REAL = "REAL";
    private static final String MODE_SIMULATED = "SIMULATED";
    private static final String MODE_SIMULATION_LEGACY = "SIMULATION";
    private static final String SOURCE_REAL = "REAL";
    private static final String SOURCE_SIMULATED = "SIMULATED";

    private final LogReader logReader = new LogReader();
    private final IntrusionDetector intrusionDetector;

    public LogsController(IntrusionDetector intrusionDetector) {
        this.intrusionDetector = intrusionDetector;
    }

    private LoadedLogs fetchWindowsLogs() {
        List<LogEntry> logs = new ArrayList<>();

        ProcessBuilder systemPB = new ProcessBuilder(
                "powershell",
                "-Command",
                "Get-WinEvent -LogName System -MaxEvents 50 | " +
                        "ForEach-Object { " +
                        "$_.TimeCreated.ToString('yyyy-MM-dd HH:mm:ss') + ' [SYSTEM-' + $_.LevelDisplayName + '] ' + ($_.Message -replace '\\r|\\n',' ') " +
                        "}"
        );
        systemPB.redirectErrorStream(true);

        ProcessBuilder securityPB = new ProcessBuilder(
                "powershell",
                "-Command",
                "Get-WinEvent -FilterHashtable @{LogName='Security'; Id=4625} -MaxEvents 20 | " +
                        "ForEach-Object { " +
                        "$_.TimeCreated.ToString('yyyy-MM-dd HH:mm:ss') + ' [SECURITY-' + $_.LevelDisplayName + '] ' + ($_.Message -replace '\\r|\\n',' ') " +
                        "}"
        );
        securityPB.redirectErrorStream(true);

        readProcessLogs("System", systemPB, logs);
        readProcessLogs("Security", securityPB, logs);

        logger.info("REAL mode fetched {} total Windows log entries from Windows Event Viewer", logs.size());
        return new LoadedLogs(MODE_REAL, SOURCE_REAL, tagLogs(logs, MODE_REAL, SOURCE_REAL));
    }

    private void readProcessLogs(String sourceName, ProcessBuilder pb, List<LogEntry> logs) {
        int parsedCount = 0;

        try {
            Process process = pb.start();

            try (BufferedReader psReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = psReader.readLine()) != null) {
                    LogEntry entry = logReader.parseLine(line);
                    if (entry != null) {
                        logs.add(entry);
                        parsedCount++;
                    } else {
                        logger.debug("Skipped unparsable {} log line: {}", sourceName, line);
                    }
                }
            }

            int exitCode = process.waitFor();
            logger.info("{} PowerShell fetch completed with exitCode={} parsedLogs={}", sourceName, exitCode, parsedCount);
        } catch (Exception e) {
            logger.error("Failed to fetch {} logs from Windows Event Viewer", sourceName, e);
        }
    }

    private LoadedLogs fetchSampleLogs() throws IOException {
        List<Path> candidates = List.of(
                Path.of("src", "main", "java", "com", "ids", "api", "logs", "sample.log"),
                Path.of("src", "main", "java", "com", "ids", "sample.log")
        );

        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                List<LogEntry> logs = logReader.readLogs(candidate.toString());
                logger.info("SIMULATED mode loaded {} log entries from {}", logs.size(), candidate);
                return new LoadedLogs(
                        MODE_SIMULATED,
                        SOURCE_SIMULATED,
                        tagLogs(logs, MODE_SIMULATED, SOURCE_SIMULATED)
                );
            }
        }

        logger.warn("SIMULATED mode requested, but no sample log file was found");
        return new LoadedLogs(MODE_SIMULATED, SOURCE_SIMULATED, List.of());
    }

    private LoadedLogs loadLogs(String mode) throws IOException {
        String normalizedMode = normalizeMode(mode);
        LoadedLogs loadedLogs = MODE_SIMULATED.equals(normalizedMode)
                ? fetchSampleLogs()
                : fetchWindowsLogs();

        logger.info(
                "Request pipeline mode={} source={} fetchedLogs={}",
                loadedLogs.mode(),
                loadedLogs.source(),
                loadedLogs.logs().size()
        );
        if (MODE_REAL.equals(loadedLogs.mode()) && loadedLogs.logs().isEmpty()) {
            logger.info("REAL mode returned no logs. No simulation fallback will be used.");
        }
        return loadedLogs;
    }

    private String normalizeMode(String mode) {
        if (mode != null && (
                mode.equalsIgnoreCase(MODE_SIMULATED) ||
                mode.equalsIgnoreCase(MODE_SIMULATION_LEGACY)
        )) {
            return MODE_SIMULATED;
        }

        return MODE_REAL;
    }

    private String normalizeSourceType(String sourceType) {
        return normalizeMode(sourceType);
    }

    private List<LogEntry> tagLogs(List<LogEntry> logs, String mode, String source) {
        return logs.stream()
                .map(log -> log.withContext(mode, source))
                .toList();
    }

    private String buildExportFileName(String prefix, String mode, String extension) {
        String safeMode = Normalizer.normalize(mode, Normalizer.Form.NFKC)
                .replaceAll("[^A-Za-z0-9_-]", "_")
                .toLowerCase();
        return prefix + "_" + safeMode + "." + extension;
    }

    @GetMapping("/api/logs")
    public List<LogEntry> getLogs(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = MODE_REAL) String mode,
            @RequestParam(required = false) String sourceType,
            @RequestParam(name = "source_type", required = false) String sourceTypeLegacy
    ) throws IOException {
        String requestedSourceType = sourceType != null ? sourceType : sourceTypeLegacy;
        String effectiveMode = requestedSourceType != null ? requestedSourceType : mode;
        logger.info("Hit GET /api/logs with mode={} sourceType={} type={}", mode, requestedSourceType, type);
        LoadedLogs loadedLogs = loadLogs(effectiveMode);
        String normalizedSourceType = normalizeSourceType(requestedSourceType != null ? requestedSourceType : loadedLogs.source());
        List<LogEntry> logs = loadedLogs.logs().stream()
                .filter(log -> normalizedSourceType.equalsIgnoreCase(log.getSourceType()))
                .toList();

        if (type == null || type.isEmpty()) {
            return logs;
        }

        return logs.stream()
                .filter(log -> log.getEventType() != null
                        && log.getEventType().toUpperCase().contains(type.toUpperCase()))
                .toList();
    }

    @GetMapping("/api/alerts")
    public List<Alert> getAlerts(@RequestParam(defaultValue = MODE_REAL) String mode) throws IOException {

        logger.info("Hit GET /api/alerts with mode={}", mode);

        // Step 1: Detection alerts (existing)
        LoadedLogs loadedLogs = loadLogs(mode);
        List<Alert> detectionAlerts = intrusionDetector.detect(
                loadedLogs.mode(),
                loadedLogs.source(),
                loadedLogs.logs()
        );

        // Step 2: FIM alerts from DB
        DatabaseHelper dbHelper = new DatabaseHelper();
        List<Alert> fimAlerts = dbHelper.fetchAllAlerts();

        // Step 3: Merge both
        List<Alert> allAlerts = new ArrayList<>();
        allAlerts.addAll(detectionAlerts);
        allAlerts.addAll(fimAlerts);

        logger.info("Total alerts returned: {}", allAlerts.size());

        return allAlerts;
    }

    @GetMapping("/api/timeline")
    public List<LogEntry> getTimeline(@RequestParam(defaultValue = MODE_REAL) String mode) throws IOException {
        logger.info("Hit GET /api/timeline with mode={}", mode);
        return loadLogs(mode).logs();
    }

    @GetMapping("/api/system-stats")
    public Map<String, String> getSystemStats() throws IOException {
        logger.info("Hit GET /api/system-stats");
        ProcessBuilder pb = new ProcessBuilder(
                "powershell",
                "-Command",
                "Get-Counter '\\Processor(_Total)\\% Processor Time'," +
                        "'\\Memory\\% Committed Bytes In Use'," +
                        "'\\Network Interface(*)\\Bytes Total/sec' | " +
                        "Select -ExpandProperty CounterSamples | " +
                        "Group-Object Path | " +
                        "ForEach-Object { ($_.Group | Measure-Object CookedValue -Sum).Sum }"
        );

        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        List<Double> values = new ArrayList<>();
        String line;

        while ((line = reader.readLine()) != null) {
            try {
                values.add(Double.parseDouble(line.trim()));
            } catch (Exception ignored) {
            }
        }

        double cpu = values.size() > 0 ? values.get(0) : 0;
        double memory = values.size() > 1 ? values.get(1) : 0;
        double network = values.size() > 2 ? values.get(2) : 0;

        return Map.of(
                "cpu", String.valueOf((int) cpu),
                "memory", String.valueOf((int) memory),
                "network", String.valueOf((int) (network / 1024))
        );
    }

    @GetMapping("/api/export/pdf")
    public ResponseEntity<InputStreamResource> exportPDF(
            @RequestParam(defaultValue = MODE_REAL) String mode
    ) throws Exception {
        logger.info("Hit GET /api/export/pdf with mode={}", mode);
        LoadedLogs loadedLogs = loadLogs(mode);
        List<LogEntry> logs = loadedLogs.logs();
        List<Alert> alerts = intrusionDetector.detect(loadedLogs.mode(), loadedLogs.source(), logs);

        PDFGenerator pdfGenerator = new PDFGenerator();
        String filePath = buildExportFileName("report", loadedLogs.mode(), "pdf");
        pdfGenerator.generateReport(logs, alerts, filePath, loadedLogs.mode(), loadedLogs.source());

        File file = new File(filePath);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(file.length())
                .body(resource);
    }

    @GetMapping("/api/export/csv")
    public ResponseEntity<InputStreamResource> exportCSV(
            @RequestParam(defaultValue = MODE_REAL) String mode
    ) throws Exception {
        logger.info("Hit GET /api/export/csv with mode={}", mode);
        LoadedLogs loadedLogs = loadLogs(mode);
        List<LogEntry> logs = loadedLogs.logs();

        CSVExporter exporter = new CSVExporter();
        String filePath = buildExportFileName("logs_export", loadedLogs.mode(), "csv");
        exporter.exportLogs(logs, filePath);

        File file = new File(filePath);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.length())
                .body(resource);
    }

    private record LoadedLogs(String mode, String source, List<LogEntry> logs) {
    }
}
