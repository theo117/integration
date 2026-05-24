package com.businesshub.dashboard.integration;

import com.businesshub.dashboard.service.LeadCsvImportService;
import com.businesshub.dashboard.web.response.LeadImportResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class CsvLeadAdapter {

    private final LeadCsvImportService leadCsvImportService;

    public CsvLeadAdapter(LeadCsvImportService leadCsvImportService) {
        this.leadCsvImportService = leadCsvImportService;
    }

    public LeadImportResponse importFile(MultipartFile file) {
        return leadCsvImportService.importCsv(file);
    }
}
