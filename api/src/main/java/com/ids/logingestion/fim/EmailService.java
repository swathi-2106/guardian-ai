package com.ids.logingestion.fim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String SUBJECT = "Security Alert: File Change Detected";

    private final JavaMailSender mailSender;
    private final boolean emailEnabled;
    private final String fromAddress;
    private final String toAddress;
    private final long failureBackoffMs;
    private volatile long nextRetryAtMs;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${ids.alert.email.enabled:false}") boolean emailEnabled,
            @Value("${ids.alert.email.from:}") String fromAddress,
            @Value("${ids.alert.email.to:}") String toAddress,
            @Value("${ids.alert.email.failure-backoff-ms:300000}") long failureBackoffMs
    ) {
        this.mailSender = mailSender;
        this.emailEnabled = emailEnabled;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.failureBackoffMs = failureBackoffMs;
    }

    public void sendFileChangeAlert(String fileName, String changeType, String timestamp, String severity) {
        if (!emailEnabled) {
            logger.info("FIM email skipped because ids.alert.email.enabled=false for file {}", fileName);
            return;
        }

        if (toAddress == null || toAddress.isBlank()) {
            logger.warn("FIM email skipped because ids.alert.email.to is blank for file {}", fileName);
            return;
        }

        long currentTimeMs = System.currentTimeMillis();
        // Debug mode: always attempt email so SMTP failures are visible immediately.
        // if (currentTimeMs < nextRetryAtMs) {
        //     logger.info("FIM email skipped during SMTP failure backoff for file {}", fileName);
        //     return;
        // }

        try {
            System.out.println("Attempting to send email...");
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toAddress);
            if (fromAddress != null && !fromAddress.isBlank()) {
                message.setFrom(fromAddress);
            }
            message.setSubject(SUBJECT);
            message.setText(buildBody(fileName, changeType, timestamp, severity));

            mailSender.send(message);
            nextRetryAtMs = 0;
            System.out.println("Email sent successfully");
            logger.info("FIM email sent for {} change on {}", changeType, fileName);
        } catch (Exception e) {
            nextRetryAtMs = currentTimeMs + failureBackoffMs;

            System.out.println("EMAIL FAILED: " + e.getMessage());
            e.printStackTrace();

            logger.warn("Failed to send FIM email: {}", e.getMessage());
        }
    }

    private String buildBody(String fileName, String changeType, String timestamp, String severity) {
        return "File integrity monitoring detected a file change.\n\n"
                + "File Name: " + safe(fileName) + "\n"
                + "Change Type: " + safe(changeType) + "\n"
                + "Timestamp: " + safe(timestamp) + "\n"
                + "Severity: " + safe(severity) + "\n";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

}
