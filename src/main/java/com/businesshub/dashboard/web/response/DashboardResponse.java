package com.businesshub.dashboard.web.response;

import com.businesshub.dashboard.domain.LeadStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardResponse(
        long totalLeads,
        long pendingInvoices,
        long overdueInvoices,
        BigDecimal openRevenue,
        Map<LeadStatus, Long> leadPipeline,
        List<LeadResponse> recentLeads,
        List<InvoiceResponse> recentInvoices,
        List<NotificationResponse> notifications,
        long unreadNotifications
) {
}
