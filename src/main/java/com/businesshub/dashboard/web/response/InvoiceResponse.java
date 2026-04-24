package com.businesshub.dashboard.web.response;

import com.businesshub.dashboard.domain.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record InvoiceResponse(
        Long id,
        String invoiceNumber,
        String clientName,
        BigDecimal amount,
        LocalDate dueDate,
        InvoiceStatus status,
        LeadSummaryResponse lead,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
