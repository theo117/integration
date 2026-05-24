package com.businesshub.dashboard.web.response;

import com.businesshub.dashboard.domain.AppUser;
import com.businesshub.dashboard.domain.AppNotification;
import com.businesshub.dashboard.domain.IntegrationEvent;
import com.businesshub.dashboard.domain.Invoice;
import com.businesshub.dashboard.domain.Lead;
import com.businesshub.dashboard.service.DashboardService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApiResponseMapper {

    public LeadResponse toLeadResponse(Lead lead) {
        return new LeadResponse(
                lead.getId(),
                lead.getName(),
                lead.getEmail(),
                lead.getPhone(),
                lead.getCompany(),
                lead.getSource(),
                lead.getStatus(),
                lead.getNotes(),
                lead.getCreatedAt(),
                lead.getUpdatedAt()
        );
    }

    public List<LeadResponse> toLeadResponses(List<Lead> leads) {
        return leads.stream().map(this::toLeadResponse).toList();
    }

    public InvoiceResponse toInvoiceResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getClientName(),
                invoice.getAmount(),
                invoice.getDueDate(),
                invoice.getStatus(),
                invoice.getLead() == null ? null : toLeadSummary(invoice.getLead()),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }

    public List<InvoiceResponse> toInvoiceResponses(List<Invoice> invoices) {
        return invoices.stream().map(this::toInvoiceResponse).toList();
    }

    public NotificationResponse toNotificationResponse(AppNotification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }

    public DashboardResponse toDashboardResponse(DashboardService.DashboardView view) {
        return new DashboardResponse(
                view.totalLeads(),
                view.pendingInvoices(),
                view.overdueInvoices(),
                view.openRevenue(),
                view.leadPipeline(),
                toLeadResponses(view.recentLeads()),
                toInvoiceResponses(view.recentInvoices()),
                view.notifications().stream().map(this::toNotificationResponse).toList(),
                view.unreadNotifications()
        );
    }

    public UserResponse toUserResponse(AppUser appUser) {
        return new UserResponse(
                appUser.getId(),
                appUser.getUsername(),
                appUser.getRole(),
                appUser.isActive(),
                appUser.getCreatedAt()
        );
    }

    public IntegrationEventResponse toIntegrationEventResponse(IntegrationEvent event) {
        return new IntegrationEventResponse(
                event.getId(),
                event.getDirection(),
                event.getType(),
                event.getProvider(),
                event.getStatus(),
                event.getSummary(),
                event.getDetail(),
                event.getErrorMessage(),
                event.getReferenceType(),
                event.getReferenceId(),
                event.getAttempts(),
                event.getNextRetryAt(),
                event.getCreatedAt()
        );
    }

    private LeadSummaryResponse toLeadSummary(Lead lead) {
        return new LeadSummaryResponse(lead.getId(), lead.getName(), lead.getCompany());
    }
}
