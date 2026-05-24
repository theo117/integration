package com.businesshub.dashboard.integration;

import com.businesshub.dashboard.domain.IntegrationEventStatus;
import com.businesshub.dashboard.domain.IntegrationEventType;
import com.businesshub.dashboard.domain.Lead;
import com.businesshub.dashboard.service.IntegrationEventService;
import com.businesshub.dashboard.service.LeadService;
import com.businesshub.dashboard.web.request.CreateLeadRequest;
import org.springframework.stereotype.Component;

@Component
public class WebsiteLeadAdapter {

    private final LeadService leadService;
    private final IntegrationEventService integrationEventService;

    public WebsiteLeadAdapter(LeadService leadService, IntegrationEventService integrationEventService) {
        this.leadService = leadService;
        this.integrationEventService = integrationEventService;
    }

    public Lead capture(CreateLeadRequest request) {
        Lead lead = leadService.createLead(request);
        integrationEventService.inbound(IntegrationEventType.WEBSITE_LEAD, "Dashboard form",
                IntegrationEventStatus.PROCESSED, "Lead created from dashboard form",
                lead.getName() + " / " + lead.getCompany(), "Lead", lead.getId());
        return lead;
    }
}
