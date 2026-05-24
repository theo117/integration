package com.businesshub.dashboard.service;

import com.businesshub.dashboard.domain.Invoice;
import com.businesshub.dashboard.domain.InvoiceStatus;
import com.businesshub.dashboard.repository.InvoiceRepository;
import com.businesshub.dashboard.repository.LeadRepository;
import com.businesshub.dashboard.repository.NotificationRepository;
import com.businesshub.dashboard.web.request.CreateInvoiceRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailAutomationService emailAutomationService;

    @Mock
    private IntegrationEventService integrationEventService;

    @Test
    void createInvoiceMarksPastDueInvoiceAsOverdue() {
        NotificationService notificationService = new NotificationService(notificationRepository);
        InvoiceService invoiceService = new InvoiceService(
                invoiceRepository,
                leadRepository,
                notificationService,
                emailAutomationService,
                integrationEventService
        );

        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setClientName("Craft Logic");
        request.setAmount(new BigDecimal("18500.00"));
        request.setDueDate(LocalDate.now().minusDays(2));

        when(invoiceRepository.findByStatusNotAndDueDateBefore(any(), any())).thenReturn(List.of());
        when(invoiceRepository.count()).thenReturn(0L);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setId(10L);
            return invoice;
        });
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Invoice savedInvoice = invoiceService.createInvoice(request);

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(invoiceCaptor.capture());
        Invoice captured = invoiceCaptor.getValue();

        assertThat(captured.getStatus()).isEqualTo(InvoiceStatus.OVERDUE);
        assertThat(savedInvoice.getInvoiceNumber()).startsWith("INV-");
        verify(notificationRepository).save(any());
        verify(emailAutomationService).sendInvoiceCreatedEmail(savedInvoice);
    }

    @Test
    void refreshOverdueInvoicesUpdatesOnlyNonOverdueRecords() {
        NotificationService notificationService = new NotificationService(notificationRepository);
        InvoiceService invoiceService = new InvoiceService(
                invoiceRepository,
                leadRepository,
                notificationService,
                emailAutomationService,
                integrationEventService
        );

        Invoice pendingInvoice = new Invoice();
        pendingInvoice.setInvoiceNumber("INV-001");
        pendingInvoice.setStatus(InvoiceStatus.PENDING);
        pendingInvoice.setDueDate(LocalDate.now().minusDays(1));

        Invoice alreadyOverdue = new Invoice();
        alreadyOverdue.setInvoiceNumber("INV-002");
        alreadyOverdue.setStatus(InvoiceStatus.OVERDUE);
        alreadyOverdue.setDueDate(LocalDate.now().minusDays(3));

        when(invoiceRepository.findByStatusNotAndDueDateBefore(eq(InvoiceStatus.PAID), any()))
                .thenReturn(List.of(pendingInvoice, alreadyOverdue));
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        invoiceService.refreshOverdueInvoices();

        verify(invoiceRepository).save(pendingInvoice);
        verify(notificationRepository).save(any());
        verify(invoiceRepository, never()).save(alreadyOverdue);
        verify(emailAutomationService).sendInvoiceOverdueEmail(pendingInvoice);
    }
}
