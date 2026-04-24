package com.businesshub.dashboard.service;

import com.businesshub.dashboard.domain.Invoice;
import com.businesshub.dashboard.domain.InvoiceStatus;
import com.businesshub.dashboard.domain.Lead;
import com.businesshub.dashboard.domain.LeadSource;
import com.businesshub.dashboard.domain.LeadStatus;
import com.businesshub.dashboard.repository.InvoiceRepository;
import com.businesshub.dashboard.repository.LeadRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportingService {

    private final LeadRepository leadRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;

    public ReportingService(LeadRepository leadRepository,
                            InvoiceRepository invoiceRepository,
                            InvoiceService invoiceService) {
        this.leadRepository = leadRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceService = invoiceService;
    }

    public ReportingView getReportingView() {
        invoiceService.refreshOverdueInvoices();

        YearMonth currentMonth = YearMonth.now();
        LocalDate today = LocalDate.now();

        List<Lead> allLeads = leadRepository.findAll();
        List<Invoice> allInvoices = invoiceRepository.findAll();

        List<Lead> monthlyLeads = allLeads.stream()
                .filter(lead -> YearMonth.from(lead.getCreatedAt()).equals(currentMonth))
                .toList();

        List<Invoice> monthlyInvoices = allInvoices.stream()
                .filter(invoice -> YearMonth.from(invoice.getCreatedAt()).equals(currentMonth))
                .toList();

        long monthlyLeadCount = monthlyLeads.size();
        long wonCount = monthlyLeads.stream().filter(lead -> lead.getStatus() == LeadStatus.WON).count();
        long quotedCount = monthlyLeads.stream().filter(lead -> lead.getStatus() == LeadStatus.QUOTED).count();
        long contactedCount = monthlyLeads.stream().filter(lead -> lead.getStatus() == LeadStatus.CONTACTED).count();
        long overdueInvoices = allInvoices.stream().filter(invoice -> invoice.getStatus() == InvoiceStatus.OVERDUE).count();

        BigDecimal monthlyPipelineValue = monthlyInvoices.stream()
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal collectedRevenue = monthlyInvoices.stream()
                .filter(invoice -> invoice.getStatus() == InvoiceStatus.PAID)
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<LeadSource, Long> sourceBreakdown = new LinkedHashMap<>();
        for (LeadSource source : LeadSource.values()) {
            sourceBreakdown.put(source, monthlyLeads.stream().filter(lead -> lead.getSource() == source).count());
        }

        Map<LeadStatus, Long> pipelineBreakdown = new LinkedHashMap<>();
        for (LeadStatus status : LeadStatus.values()) {
            pipelineBreakdown.put(status, monthlyLeads.stream().filter(lead -> lead.getStatus() == status).count());
        }

        Map<String, BigDecimal> invoiceBreakdown = new LinkedHashMap<>();
        invoiceBreakdown.put("Paid", allInvoices.stream()
                .filter(invoice -> invoice.getStatus() == InvoiceStatus.PAID)
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        invoiceBreakdown.put("Pending", allInvoices.stream()
                .filter(invoice -> invoice.getStatus() == InvoiceStatus.PENDING)
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        invoiceBreakdown.put("Overdue", allInvoices.stream()
                .filter(invoice -> invoice.getStatus() == InvoiceStatus.OVERDUE)
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        List<PerformanceSnapshot> snapshots = List.of(
                new PerformanceSnapshot("Monthly lead intake", monthlyLeadCount,
                        "Leads added in " + currentMonth.getMonth()),
                new PerformanceSnapshot("Quoted opportunities", quotedCount,
                        "Leads progressed to QUOTED"),
                new PerformanceSnapshot("Contact coverage", contactedCount,
                        "Leads already contacted"),
                new PerformanceSnapshot("Overdue invoices", overdueInvoices,
                        "Invoices needing follow-up as of " + today)
        );

        BigDecimal conversionRate = monthlyLeadCount == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(wonCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(monthlyLeadCount), 1, RoundingMode.HALF_UP);

        return new ReportingView(
                currentMonth.toString(),
                monthlyLeadCount,
                wonCount,
                conversionRate,
                monthlyPipelineValue,
                collectedRevenue,
                sourceBreakdown,
                pipelineBreakdown,
                invoiceBreakdown,
                snapshots
        );
    }

    public record ReportingView(
            String periodLabel,
            long monthlyLeadCount,
            long wonCount,
            BigDecimal conversionRate,
            BigDecimal monthlyPipelineValue,
            BigDecimal collectedRevenue,
            Map<LeadSource, Long> sourceBreakdown,
            Map<LeadStatus, Long> pipelineBreakdown,
            Map<String, BigDecimal> invoiceBreakdown,
            List<PerformanceSnapshot> snapshots
    ) {
    }

    public record PerformanceSnapshot(
            String label,
            long value,
            String context
    ) {
    }
}
