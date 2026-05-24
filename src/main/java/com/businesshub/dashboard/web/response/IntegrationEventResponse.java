package com.businesshub.dashboard.web.response;

import com.businesshub.dashboard.domain.IntegrationDirection;
import com.businesshub.dashboard.domain.IntegrationEventStatus;
import com.businesshub.dashboard.domain.IntegrationEventType;

import java.time.LocalDateTime;

public record IntegrationEventResponse(
        Long id,
        IntegrationDirection direction,
        IntegrationEventType type,
        String provider,
        IntegrationEventStatus status,
        String summary,
        String detail,
        String errorMessage,
        String referenceType,
        Long referenceId,
        int attempts,
        LocalDateTime nextRetryAt,
        LocalDateTime createdAt
) {
}
