package com.businesshub.dashboard.web.response;

import com.businesshub.dashboard.domain.LeadSource;
import com.businesshub.dashboard.domain.LeadStatus;

import java.time.LocalDateTime;

public record LeadResponse(
        Long id,
        String name,
        String email,
        String phone,
        String company,
        LeadSource source,
        LeadStatus status,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
