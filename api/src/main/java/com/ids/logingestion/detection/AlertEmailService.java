package com.ids.logingestion.detection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AlertEmailService {

    private static final Logger logger = LoggerFactory.getLogger(AlertEmailService.class);

    private final JavaMailSender mailSender;
    private final boolean emailEnabled;
    private final String fromAddress;
    private final String toAddress;
    private final Set<String> emailedAlertKeys = ConcurrentHashMap.newKeySet();

    public AlertEmailService(
            JavaMailSender mailSender,
            @Value("${ids.alert.email.enabled:false}") boolean emailEnabled,
            @Value("${ids.alert.email.from:}") String fromAddress,
            @Value("${ids.alert.email.to:}") String toAddress
    ) {
        this.mailSender = mailSender;
        this.emailEnabled = emailEnabled;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
    }

    public void notifyIfNew(Alert alert) {
        notifyIfNew(alert, "UNKNOWN", "UNKNOWN");
    }

    public void notifyIfNew(Alert alert, String mode, String source) {
        if (alert == null) {
            logger.warn("Email notification skipped because alert was null. mode={} source={}", mode, source);
            return;
        }

        if (!"REAL".equalsIgnoreCase(mode)) {
            logger.info("Email notification skipped for non-real alert mode={} source={} alert={}", mode, source, alert);
            return;
        }

        String alertKey = buildKey(alert);
        logger.info(
                "Email notification evaluation mode={} source={} enabled={} recipient={} alert={}",
                mode,
                source,
                emailEnabled,
                safe(toAddress),
                alert
        );

        if (!emailEnabled) {
            logger.info("Email notification skipped because ids.alert.email.enabled=false for alert {}", alertKey);
            return;
        }

        if (toAddress == null || toAddress.isBlank()) {
            logger.warn("Email notification skipped because ids.alert.email.to is blank for alert {}", alertKey);
            return;
        }

        if (!emailedAlertKeys.add(alertKey)) {
            logger.info("Skipping duplicate email for alert {}", alertKey);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toAddress);
            if (fromAddress != null && !fromAddress.isBlank()) {
                message.setFrom(fromAddress);
            }
            message.setSubject("IDS Alert Detected [REAL]");
            message.setText(buildBody(alert, mode, source));

            logger.info(
                    "Sending email for alert {} to {} from {} mode={} source={}",
                    alertKey,
                    toAddress,
                    safe(fromAddress),
                    mode,
                    source
            );
            mailSender.send(message);
            logger.info("Email sent successfully for alert {}", alertKey);
        } catch (Exception e) {
            emailedAlertKeys.remove(alertKey);
            logger.warn("Email failed for alert {}", alertKey, e);
        }
    }

    private String buildBody(Alert alert, String mode, String source) {
        return "A new IDS alert was detected.\n\n"
                + "Data Mode: " + safe(mode) + "\n"
                + "Data Source: " + safe(source) + "\n"
                + "Time: " + safe(alert.getTimestamp()) + "\n"
                + "Severity: " + safe(alert.getSeverity()) + "\n"
                + "Message: " + safe(alert.getMessage()) + "\n";
    }

    private String buildKey(Alert alert) {
        return String.join("|",
                safe(alert.getTimestamp()),
                safe(alert.getSeverity()),
                safe(alert.getMessage())
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
