package com.businesshub.dashboard.service;

import com.businesshub.dashboard.domain.Invoice;
import com.businesshub.dashboard.domain.InvoiceStatus;
import com.businesshub.dashboard.domain.Lead;
import com.businesshub.dashboard.domain.LeadSource;
import com.businesshub.dashboard.domain.LeadStatus;
import com.businesshub.dashboard.repository.InvoiceRepository;
import com.businesshub.dashboard.repository.LeadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceService invoiceService;

    @Test
    void getReportingViewBuildsMonthlyMetrics() {
        ReportingService reportingService = new ReportingService(leadRepository, invoiceRepository, invoiceService);
        LocalDateTime now = LocalDateTime.now();

        Lead newLead = new Lead();
        newLead.setName("Nandi");
        newLead.setCompany("Luma");
        newLead.setSource(LeadSource.WEBSITE);
        newLead.setStatus(LeadStatus.NEW);
        newLead.setCreatedAt(now.minusDays(2));

        Lead wonLead = new Lead();
        wonLead.setName("Aisha");
        wonLead.setCompany("Harbour");
        wonLead.setSource(LeadSource.CSV_IMPORT);
        wonLead.setStatus(LeadStatus.WON);
        wonLead.setCreatedAt(now.minusDays(1));

        Lead oldLead = new Lead();
        oldLead.setName("Old");
        oldLead.setCompany("Legacy");
        oldLead.setSource(LeadSource.WHATSAPP);
        oldLead.setStatus(LeadStatus.WON);
        oldLead.setCreatedAt(now.minusMonths(1));

        Invoice paidInvoice = new Invoice();
        paidInvoice.setAmount(new BigDecimal("10000.00"));
        paidInvoice.setStatus(InvoiceStatus.PAID);
        paidInvoice.setCreatedAt(now.minusDays(1));
        paidInvoice.setDueDate(LocalDate.now().plusDays(7));

        Invoice overdueInvoice = new Invoice();
        overdueInvoice.setAmount(new BigDecimal("5000.00"));
        overdueInvoice.setStatus(InvoiceStatus.OVERDUE);
        overdueInvoice.setCreatedAt(now.minusDays(3));
        overdueInvoice.setDueDate(LocalDate.now().minusDays(1));

        doNothing().when(invoiceService).refreshOverdueInvoices();
        when(leadRepository.findAll()).thenReturn(List.of(newLead, wonLead, oldLead));
        when(invoiceRepository.findAll()).thenReturn(List.of(paidInvoice, overdueInvoice));

        ReportingService.ReportingView view = reportingService.getReportingView();

        assertThat(view.monthlyLeadCount()).isEqualTo(2);
        assertThat(view.wonCount()).isEqualTo(1);
        assertThat(view.conversionRate()).isEqualByComparingTo("50.0");
        assertThat(view.monthlyPipelineValue()).isEqualByComparingTo("15000.00");
        assertThat(view.collectedRevenue()).isEqualByComparingTo("10000.00");
        assertThat(view.sourceBreakdown().get(LeadSource.WEBSITE)).isEqualTo(1);
        assertThat(view.sourceBreakdown().get(LeadSource.CSV_IMPORT)).isEqualTo(1);
        assertThat(view.invoiceBreakdown().get("Overdue")).isEqualByComparingTo("5000.00");
    }
}
