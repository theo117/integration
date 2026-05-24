package com.businesshub.dashboard.integration;

import com.businesshub.dashboard.domain.IntegrationEventStatus;
import com.businesshub.dashboard.service.IntegrationEventService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EmailNotificationAdapter {

    private final IntegrationEventService integrationEventService;

    public EmailNotificationAdapter(IntegrationEventService integrationEventService) {
        this.integrationEventService = integrationEventService;
    }

    public void recordSkipped(String to, String subject, String reason) {
        integrationEventService.outboundEmail(IntegrationEventStatus.SKIPPED,
                "Email skipped: " + subject, "To: " + to + ". " + reason, null, null);
    }

    public void recordSent(String to, String subject) {
        integrationEventService.outboundEmail(IntegrationEventStatus.SENT,
                "Email sent: " + subject, "To: " + to, null, null);
    }

    public void recordFailure(String to, String subject, Exception exception) {
        integrationEventService.outboundEmail(IntegrationEventStatus.FAILED,
                "Email failed: " + subject, "To: " + to, exception.getMessage(), LocalDateTime.now().plusMinutes(15));
    }
}
