package com.businesshub.dashboard.service;

import com.businesshub.dashboard.domain.Invoice;
import com.businesshub.dashboard.domain.InvoiceStatus;
import com.businesshub.dashboard.domain.Lead;
import com.businesshub.dashboard.domain.NotificationType;
import com.businesshub.dashboard.repository.InvoiceRepository;
import com.businesshub.dashboard.repository.LeadRepository;
import com.businesshub.dashboard.web.request.CreateInvoiceRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final LeadRepository leadRepository;
    private final NotificationService notificationService;
    private final EmailAutomationService emailAutomationService;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          LeadRepository leadRepository,
                          NotificationService notificationService,
                          EmailAutomationService emailAutomationService) {
        this.invoiceRepository = invoiceRepository;
        this.leadRepository = leadRepository;
        this.notificationService = notificationService;
        this.emailAutomationService = emailAutomationService;
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public List<Invoice> getRecentInvoices() {
        refreshOverdueInvoices();
        return invoiceRepository.findTop8ByOrderByCreatedAtDesc();
    }

    @Transactional
    public Invoice createInvoice(CreateInvoiceRequest request) {
        refreshOverdueInvoices();

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setClientName(request.getClientName());
        invoice.setAmount(request.getAmount());
        invoice.setDueDate(request.getDueDate());
        invoice.setStatus(request.getDueDate().isBefore(LocalDate.now()) ? InvoiceStatus.OVERDUE : InvoiceStatus.PENDING);

        if (request.getLeadId() != null) {
            Lead lead = leadRepository.findById(request.getLeadId())
                    .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + request.getLeadId()));
            invoice.setLead(lead);
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);
        notificationService.create(NotificationType.INFO,
                "Invoice " + savedInvoice.getInvoiceNumber() + " created for " + savedInvoice.getClientName());
        emailAutomationService.sendInvoiceCreatedEmail(savedInvoice);
        return savedInvoice;
    }

    @Transactional
    public Invoice updateStatus(Long invoiceId, InvoiceStatus status) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));
        invoice.setStatus(status);
        Invoice updatedInvoice = invoiceRepository.save(invoice);

        NotificationType type = status == InvoiceStatus.PAID ? NotificationType.SUCCESS : NotificationType.WARNING;
        notificationService.create(type,
                "Invoice " + updatedInvoice.getInvoiceNumber() + " marked as " + status);
        if (status == InvoiceStatus.PAID) {
            emailAutomationService.sendInvoicePaidEmail(updatedInvoice);
        }
        return updatedInvoice;
    }

    @Transactional
    public void refreshOverdueInvoices() {
        List<Invoice> overdueInvoices = invoiceRepository.findByStatusNotAndDueDateBefore(InvoiceStatus.PAID, LocalDate.now());
        for (Invoice invoice : overdueInvoices) {
            if (invoice.getStatus() != InvoiceStatus.OVERDUE) {
                invoice.setStatus(InvoiceStatus.OVERDUE);
                invoiceRepository.save(invoice);
                notificationService.create(NotificationType.WARNING,
                        "Invoice " + invoice.getInvoiceNumber() + " is overdue");
                emailAutomationService.sendInvoiceOverdueEmail(invoice);
            }
        }
    }

    private String generateInvoiceNumber() {
        long next = invoiceRepository.count() + 1;
        return "INV-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + String.format("%03d", next);
    }
}
