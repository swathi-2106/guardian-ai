package com.ids.logingestion.fim;

import com.ids.logingestion.detection.Alert;
import com.ids.logingestion.db.DatabaseHelper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class FileMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(FileMonitorService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String SOURCE_TYPE = "REAL";
    private static final String DATA_SOURCE = "REAL";

    private static final Map<String, String> SAMPLE_FILES = Map.of(
            "config.txt", "app.mode=secure",
            "users.txt", "admin:1234",
            "secrets.txt", "API_KEY=XYZ123"
    );

    private final Path sensitiveDirectory;
    private final EmailService emailService;
    private final DatabaseHelper databaseHelper;
    private final Map<Path, FileSnapshot> previousSnapshots = new HashMap<>();
    private final List<String> changeHistory = new ArrayList<>();

    public FileMonitorService(
            @Value("${ids.fim.directory:sensitive-files}") String sensitiveDirectory,
            EmailService emailService
    ) {
        this.sensitiveDirectory = Path.of(sensitiveDirectory).toAbsolutePath().normalize();
        this.emailService = emailService;
        this.databaseHelper = new DatabaseHelper();
    }

    @PostConstruct
    public void initialize() {
        try {
            databaseHelper.initializeDatabase();
            createSensitiveDirectoryAndSamples();
            previousSnapshots.putAll(scanCurrentFiles());
            System.out.println("FIM baseline stored for directory: " + sensitiveDirectory);
            System.out.println("FIM baseline file count: " + previousSnapshots.size());
            logger.info("FIM initialized for {} with {} tracked files", sensitiveDirectory, previousSnapshots.size());
        } catch (IOException e) {
            System.out.println("FIM initialization failed: " + e.getMessage());
            logger.error("FIM initialization failed for {}", sensitiveDirectory, e);
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void scanSensitiveFiles() {
        System.out.println("FIM scan running...");
        System.out.println("FIM scanning path: " + sensitiveDirectory);

        try {
            Map<Path, FileSnapshot> currentSnapshots = scanCurrentFiles();
            detectDeletedFiles(currentSnapshots);
            detectCreatedOrModifiedFiles(currentSnapshots);
            previousSnapshots.clear();
            previousSnapshots.putAll(currentSnapshots);
        } catch (IOException e) {
            System.out.println("FIM scan failed: " + e.getMessage());
            logger.warn("FIM scan failed for {}", sensitiveDirectory, e);
        }
    }

    private void createSensitiveDirectoryAndSamples() throws IOException {
        Files.createDirectories(sensitiveDirectory);

        for (Map.Entry<String, String> sampleFile : SAMPLE_FILES.entrySet()) {
            Path file = sensitiveDirectory.resolve(sampleFile.getKey());
            if (Files.notExists(file)) {
                Files.writeString(file, sampleFile.getValue(), StandardCharsets.UTF_8);
                System.out.println("Created sample sensitive file: " + file);
                logger.info("Created FIM sample file {}", file);
            }
        }
    }

    private Map<Path, FileSnapshot> scanCurrentFiles() throws IOException {
        Map<Path, FileSnapshot> snapshots = new LinkedHashMap<>();

        try (var files = Files.list(sensitiveDirectory)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                Path normalizedFile = file.toAbsolutePath().normalize();
                String fileName = normalizedFile.getFileName().toString();
                long size = Files.size(normalizedFile);
                long lastModified = Files.getLastModifiedTime(normalizedFile).toMillis();
                String newHash = FileHashUtil.sha256(normalizedFile);
                FileSnapshot previousSnapshot = previousSnapshots.get(normalizedFile);
                String oldHash = previousSnapshot == null ? null : previousSnapshot.hash();

                System.out.println("Scanning file: " + fileName);
                System.out.println("Old hash: " + oldHash);
                System.out.println("New hash: " + newHash);

                snapshots.put(normalizedFile, new FileSnapshot(newHash, lastModified, size));
            }
        }

        return snapshots;
    }

    private void detectDeletedFiles(Map<Path, FileSnapshot> currentSnapshots) {
        for (Path previousFile : previousSnapshots.keySet()) {
            if (!currentSnapshots.containsKey(previousFile)) {
                System.out.println("File deleted detected: " + previousFile.getFileName());
                triggerAlert(previousFile.getFileName().toString(), "FILE DELETED");
            }
        }
    }

    private void detectCreatedOrModifiedFiles(Map<Path, FileSnapshot> currentSnapshots) {
        for (Map.Entry<Path, FileSnapshot> currentEntry : currentSnapshots.entrySet()) {
            Path file = currentEntry.getKey();
            FileSnapshot previousSnapshot = previousSnapshots.get(file);

            if (previousSnapshot == null) {
                System.out.println("New file detected: " + file.getFileName());
                triggerAlert(file.getFileName().toString(), "FILE CREATED");
                continue;
            }

            if (!previousSnapshot.hash().equals(currentEntry.getValue().hash())) {
                System.out.println("File modified detected: " + file.getFileName());
                triggerAlert(file.getFileName().toString(), "FILE MODIFIED");
            } else {
                System.out.println("No change detected for file: " + file.getFileName());
            }
        }
    }

    private void triggerAlert(String fileName, String changeType) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String severity = severityFor(fileName);
        String message = changeType.toLowerCase(Locale.ROOT).replace('_', ' ') + ": " + fileName;
        Alert alert = new Alert(timestamp, severity, message, List.of(), SOURCE_TYPE, DATA_SOURCE);

        System.out.println("Creating alert for file: " + fileName);
        System.out.println("Alert change type: " + changeType);
        System.out.println("Alert severity: " + severity);
        databaseHelper.insertAlert(alert);
        System.out.println("Alert saved to database for file: " + fileName);

        System.out.println("Sending email alert...");
        emailService.sendFileChangeAlert(fileName, changeType, timestamp, severity);

        String historyEntry = timestamp + " | " + severity + " | " + changeType + " | " + fileName;
        changeHistory.add(historyEntry);
        logger.warn("FIM alert generated: {}", historyEntry);
    }

    private String severityFor(String fileName) {
        return "secrets.txt".equalsIgnoreCase(fileName) ? "CRITICAL" : "HIGH";
    }

    private record FileSnapshot(String hash, long lastModified, long size) {
    }
}
