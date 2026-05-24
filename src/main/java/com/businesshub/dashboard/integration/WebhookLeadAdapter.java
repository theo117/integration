package com.businesshub.dashboard.integration;

import com.businesshub.dashboard.config.IntegrationProperties;
import com.businesshub.dashboard.domain.IntegrationEventStatus;
import com.businesshub.dashboard.domain.IntegrationEventType;
import com.businesshub.dashboard.domain.Lead;
import com.businesshub.dashboard.domain.LeadSource;
import com.businesshub.dashboard.service.IntegrationEventService;
import com.businesshub.dashboard.service.LeadService;
import com.businesshub.dashboard.web.request.CreateLeadRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class WebhookLeadAdapter {

    private final LeadService leadService;
    private final IntegrationEventService integrationEventService;
    private final IntegrationProperties integrationProperties;

    public WebhookLeadAdapter(LeadService leadService,
                              IntegrationEventService integrationEventService,
                              IntegrationProperties integrationProperties) {
        this.leadService = leadService;
        this.integrationEventService = integrationEventService;
        this.integrationProperties = integrationProperties;
    }

    public Lead capture(CreateLeadRequest request, String apiKey) {
        validateApiKey(apiKey);
        request.setSource(LeadSource.WEBHOOK);
        Lead lead = leadService.createLead(request);
        integrationEventService.inbound(IntegrationEventType.WEBHOOK_LEAD, "External webhook",
                IntegrationEventStatus.PROCESSED, "Lead accepted from signed webhook",
                lead.getName() + " / " + lead.getCompany(), "Lead", lead.getId());
        return lead;
    }

    private void validateApiKey(String apiKey) {
        if (!StringUtils.hasText(apiKey) || !apiKey.equals(integrationProperties.getWebhookApiKey())) {
            integrationEventService.inbound(IntegrationEventType.WEBHOOK_LEAD, "External webhook",
                    IntegrationEventStatus.FAILED, "Webhook rejected: invalid API key",
                    "Missing or invalid X-Integration-Key header", null, null);
            throw new SecurityException("Invalid webhook API key.");
        }
    }
}
