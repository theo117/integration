package com.businesshub.dashboard.web.response;

import com.businesshub.dashboard.domain.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String message,
        boolean read,
        LocalDateTime createdAt
) {
}
