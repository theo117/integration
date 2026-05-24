package com.businesshub.dashboard.service;

import com.businesshub.dashboard.domain.Lead;
import com.businesshub.dashboard.domain.LeadSource;
import com.businesshub.dashboard.domain.LeadStatus;
import com.businesshub.dashboard.domain.IntegrationEventStatus;
import com.businesshub.dashboard.domain.IntegrationEventType;
import com.businesshub.dashboard.domain.NotificationType;
import com.businesshub.dashboard.repository.LeadRepository;
import com.businesshub.dashboard.web.request.CreateLeadRequest;
import com.businesshub.dashboard.web.response.LeadImportResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class LeadCsvImportService {

    private final LeadRepository leadRepository;
    private final NotificationService notificationService;
    private final IntegrationEventService integrationEventService;

    public LeadCsvImportService(LeadRepository leadRepository,
                                NotificationService notificationService,
                                IntegrationEventService integrationEventService) {
        this.leadRepository = leadRepository;
        this.notificationService = notificationService;
        this.integrationEventService = integrationEventService;
    }

    public LeadImportResponse importCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please upload a CSV file.");
        }

        List<String> errors = new ArrayList<>();
        int importedCount = 0;
        int skippedCount = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                throw new IllegalArgumentException("The CSV file is empty.");
            }

            Map<String, Integer> headerIndex = parseHeader(headerLine);
            validateRequiredHeaders(headerIndex);

            String line;
            int rowNumber = 1;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (line.isBlank()) {
                    continue;
                }

                try {
                    CreateLeadRequest request = parseLead(line, headerIndex);
                    Lead lead = toLead(request);
                    leadRepository.save(lead);
                    importedCount++;
                } catch (IllegalArgumentException exception) {
                    skippedCount++;
                    errors.add("Row " + rowNumber + ": " + exception.getMessage());
                }
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not read the uploaded CSV file.");
        }

        notificationService.create(NotificationType.INFO,
                "CSV import completed: " + importedCount + " leads imported, " + skippedCount + " skipped");
        integrationEventService.inbound(IntegrationEventType.CSV_IMPORT, "CSV upload",
                IntegrationEventStatus.PROCESSED, "CSV import completed",
                importedCount + " imported, " + skippedCount + " skipped", null, null);

        return new LeadImportResponse(importedCount, skippedCount, errors);
    }

    private Map<String, Integer> parseHeader(String headerLine) {
        List<String> values = splitCsvLine(headerLine);
        Map<String, Integer> headerIndex = new HashMap<>();
        for (int i = 0; i < values.size(); i++) {
            headerIndex.put(values.get(i).trim().toLowerCase(Locale.ENGLISH), i);
        }
        return headerIndex;
    }

    private void validateRequiredHeaders(Map<String, Integer> headerIndex) {
        List<String> requiredHeaders = List.of("name", "email", "phone", "company");
        List<String> missingHeaders = requiredHeaders.stream()
                .filter(header -> !headerIndex.containsKey(header))
                .toList();

        if (!missingHeaders.isEmpty()) {
            throw new IllegalArgumentException("Missing required CSV columns: " + String.join(", ", missingHeaders));
        }
    }

    private CreateLeadRequest parseLead(String line, Map<String, Integer> headerIndex) {
        List<String> values = splitCsvLine(line);

        CreateLeadRequest request = new CreateLeadRequest();
        request.setName(requiredValue(values, headerIndex, "name"));
        request.setEmail(requiredValue(values, headerIndex, "email"));
        request.setPhone(requiredValue(values, headerIndex, "phone"));
        request.setCompany(requiredValue(values, headerIndex, "company"));
        request.setNotes(optionalValue(values, headerIndex, "notes"));
        request.setSource(parseSource(optionalValue(values, headerIndex, "source")));
        return request;
    }

    private Lead toLead(CreateLeadRequest request) {
        Lead lead = new Lead();
        lead.setName(request.getName());
        lead.setEmail(request.getEmail());
        lead.setPhone(request.getPhone());
        lead.setCompany(request.getCompany());
        lead.setSource(request.getSource() == null ? LeadSource.CSV_IMPORT : request.getSource());
        lead.setStatus(LeadStatus.NEW);
        lead.setNotes(request.getNotes());
        return lead;
    }

    private String requiredValue(List<String> values, Map<String, Integer> headerIndex, String column) {
        String value = optionalValue(values, headerIndex, column);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing value for '" + column + "'");
        }
        return value;
    }

    private String optionalValue(List<String> values, Map<String, Integer> headerIndex, String column) {
        Integer index = headerIndex.get(column);
        if (index == null || index >= values.size()) {
            return null;
        }
        String value = values.get(index).trim();
        return value.isEmpty() ? null : value;
    }

    private LeadSource parseSource(String sourceValue) {
        if (sourceValue == null) {
            return LeadSource.CSV_IMPORT;
        }
        try {
            return LeadSource.valueOf(sourceValue.trim().toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid source '" + sourceValue + "'");
        }
    }

    private List<String> splitCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        if (inQuotes) {
            throw new IllegalArgumentException("Malformed CSV row: missing closing quote");
        }

        values.add(current.toString());
        return values;
    }
}
