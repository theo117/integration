package com.businesshub.dashboard.web.api;

import com.businesshub.dashboard.integration.CsvLeadAdapter;
import com.businesshub.dashboard.integration.WebhookLeadAdapter;
import com.businesshub.dashboard.integration.WebsiteLeadAdapter;
import com.businesshub.dashboard.service.LeadService;
import com.businesshub.dashboard.web.request.CreateLeadRequest;
import com.businesshub.dashboard.web.request.UpdateLeadStatusRequest;
import com.businesshub.dashboard.web.response.ApiResponseMapper;
import com.businesshub.dashboard.web.response.LeadImportResponse;
import com.businesshub.dashboard.web.response.LeadResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
public class LeadApiController {

    private final LeadService leadService;
    private final WebsiteLeadAdapter websiteLeadAdapter;
    private final WebhookLeadAdapter webhookLeadAdapter;
    private final CsvLeadAdapter csvLeadAdapter;
    private final ApiResponseMapper apiResponseMapper;

    public LeadApiController(LeadService leadService,
                             WebsiteLeadAdapter websiteLeadAdapter,
                             WebhookLeadAdapter webhookLeadAdapter,
                             CsvLeadAdapter csvLeadAdapter,
                             ApiResponseMapper apiResponseMapper) {
        this.leadService = leadService;
        this.websiteLeadAdapter = websiteLeadAdapter;
        this.webhookLeadAdapter = webhookLeadAdapter;
        this.csvLeadAdapter = csvLeadAdapter;
        this.apiResponseMapper = apiResponseMapper;
    }

    @GetMapping
    public List<LeadResponse> getLeads() {
        return apiResponseMapper.toLeadResponses(leadService.getAllLeads());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeadResponse createLead(@Valid @RequestBody CreateLeadRequest request) {
        return apiResponseMapper.toLeadResponse(websiteLeadAdapter.capture(request));
    }

    @PostMapping("/webhook")
    @ResponseStatus(HttpStatus.CREATED)
    public LeadResponse createLeadFromWebhook(@Valid @RequestBody CreateLeadRequest request,
                                             @RequestHeader(value = "X-Integration-Key", required = false) String apiKey) {
        return apiResponseMapper.toLeadResponse(webhookLeadAdapter.capture(request, apiKey));
    }

    @PostMapping(path = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public LeadImportResponse importLeadsFromCsv(@RequestParam("file") MultipartFile file) {
        return csvLeadAdapter.importFile(file);
    }

    @PatchMapping("/{leadId}/status")
    public LeadResponse updateLeadStatus(@PathVariable Long leadId,
                                         @Valid @RequestBody UpdateLeadStatusRequest request) {
        return apiResponseMapper.toLeadResponse(leadService.updateStatus(leadId, request.getStatus()));
    }
}
