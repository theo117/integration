package com.businesshub.dashboard.service;

import com.businesshub.dashboard.domain.AppNotification;
import com.businesshub.dashboard.domain.Invoice;
import com.businesshub.dashboard.domain.InvoiceStatus;
import com.businesshub.dashboard.domain.Lead;
import com.businesshub.dashboard.domain.LeadStatus;
import com.businesshub.dashboard.repository.InvoiceRepository;
import com.businesshub.dashboard.repository.LeadRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final LeadRepository leadRepository;
    private final InvoiceRepository invoiceRepository;
    private final LeadService leadService;
    private final InvoiceService invoiceService;
    private final NotificationService notificationService;

    public DashboardService(LeadRepository leadRepository,
                            InvoiceRepository invoiceRepository,
                            LeadService leadService,
                            InvoiceService invoiceService,
                            NotificationService notificationService) {
        this.leadRepository = leadRepository;
        this.invoiceRepository = invoiceRepository;
        this.leadService = leadService;
        this.invoiceService = invoiceService;
        this.notificationService = notificationService;
    }

    public DashboardView getDashboardView() {
        invoiceService.refreshOverdueInvoices();

        List<Lead> recentLeads = leadService.getRecentLeads();
        List<Invoice> recentInvoices = invoiceService.getRecentInvoices();
        List<AppNotification> notifications = notificationService.getRecentNotifications();

        long totalLeads = leadRepository.count();
        long pendingInvoices = invoiceRepository.countByStatus(InvoiceStatus.PENDING);
        long overdueInvoices = invoiceRepository.countByStatus(InvoiceStatus.OVERDUE);

        BigDecimal openRevenue = invoiceRepository.findAll().stream()
                .filter(invoice -> invoice.getStatus() != InvoiceStatus.PAID)
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<LeadStatus, Long> leadPipeline = new LinkedHashMap<>();
        for (LeadStatus status : LeadStatus.values()) {
            leadPipeline.put(status, leadRepository.countByStatus(status));
        }

        return new DashboardView(totalLeads, pendingInvoices, overdueInvoices, openRevenue, leadPipeline,
                recentLeads, recentInvoices, notifications, notificationService.unreadCount());
    }

    public record DashboardView(
            long totalLeads,
            long pendingInvoices,
            long overdueInvoices,
            BigDecimal openRevenue,
            Map<LeadStatus, Long> leadPipeline,
            List<Lead> recentLeads,
            List<Invoice> recentInvoices,
            List<AppNotification> notifications,
            long unreadNotifications
    ) {
    }
}
