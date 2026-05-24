package com.businesshub.dashboard.service;

import com.businesshub.dashboard.domain.IntegrationDirection;
import com.businesshub.dashboard.domain.IntegrationEvent;
import com.businesshub.dashboard.domain.IntegrationEventStatus;
import com.businesshub.dashboard.domain.IntegrationEventType;
import com.businesshub.dashboard.repository.IntegrationEventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IntegrationEventService {

    private final IntegrationEventRepository integrationEventRepository;

    public IntegrationEventService(IntegrationEventRepository integrationEventRepository) {
        this.integrationEventRepository = integrationEventRepository;
    }

    public List<IntegrationEvent> getRecentEvents() {
        return integrationEventRepository.findTop25ByOrderByCreatedAtDesc();
    }

    public IntegrationEvent record(IntegrationDirection direction,
                                   IntegrationEventType type,
                                   String provider,
                                   IntegrationEventStatus status,
                                   String summary,
                                   String detail,
                                   String errorMessage,
                                   String referenceType,
                                   Long referenceId,
                                   int attempts,
                                   LocalDateTime nextRetryAt) {
        IntegrationEvent event = new IntegrationEvent();
        event.setDirection(direction);
        event.setType(type);
        event.setProvider(provider);
        event.setStatus(status);
        event.setSummary(limit(summary, 500));
        event.setDetail(limit(detail, 2000));
        event.setErrorMessage(limit(errorMessage, 1000));
        event.setReferenceType(referenceType);
        event.setReferenceId(referenceId);
        event.setAttempts(attempts);
        event.setNextRetryAt(nextRetryAt);
        return integrationEventRepository.save(event);
    }

    public IntegrationEvent inbound(IntegrationEventType type,
                                    String provider,
                                    IntegrationEventStatus status,
                                    String summary,
                                    String detail,
                                    String referenceType,
                                    Long referenceId) {
        return record(IntegrationDirection.INBOUND, type, provider, status, summary, detail, null,
                referenceType, referenceId, 1, null);
    }

    public IntegrationEvent outboundEmail(IntegrationEventStatus status,
                                          String summary,
                                          String detail,
                                          String errorMessage,
                                          LocalDateTime nextRetryAt) {
        return record(IntegrationDirection.OUTBOUND, IntegrationEventType.EMAIL_NOTIFICATION, "SMTP",
                status, summary, detail, errorMessage, null, null, status == IntegrationEventStatus.FAILED ? 1 : 0,
                nextRetryAt);
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }
}
