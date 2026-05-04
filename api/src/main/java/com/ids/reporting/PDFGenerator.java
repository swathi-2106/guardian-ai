package com.ids.reporting;

import com.ids.logingestion.LogEntry;
import com.ids.logingestion.detection.Alert;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;

import java.io.IOException;
import java.util.List;

public class PDFGenerator {

    public void generateReport(
            List<LogEntry> logs,
            List<Alert> alerts,
            String filePath,
            String dataMode,
            String dataSource
    ) throws IOException {

        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // 🔹 Title
        document.add(new Paragraph("Intrusion Detection Report")
                .setBold()
                .setFontSize(18));

        document.add(new Paragraph("\n"));

        // 🔹 Attack Summary
        document.add(new Paragraph("Attack Summary")
                .setBold()
                .setFontSize(14));

        document.add(new Paragraph("Data Mode: " + dataMode));
        document.add(new Paragraph("Data Source: " + dataSource));
        document.add(new Paragraph("Total Logs: " + logs.size()));
        document.add(new Paragraph("Total Alerts: " + alerts.size()));

        document.add(new Paragraph("\n"));

        // 🔹 Alerts Section
        document.add(new Paragraph("Alerts")
                .setBold()
                .setFontSize(14));

        document.add(new Paragraph("\n"));

// 🔹 LOOP THROUGH ALERTS
        for (Alert alert : alerts) {

            document.add(new Paragraph(
                    "Time: " + alert.getTimestamp() +
                            "\nData Mode: " + alert.getDataMode() +
                            "\nData Source: " + alert.getDataSource() +
                            "\nSeverity: " + alert.getSeverity() +
                            "\nMessage: " + (alert.getMessage() != null ? alert.getMessage() : "No message") +
                            "\n-------------------------\n"
            ));
        }

        document.close();
    }

}
