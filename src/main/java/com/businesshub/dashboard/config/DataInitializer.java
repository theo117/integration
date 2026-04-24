package com.businesshub.dashboard.config;

import com.businesshub.dashboard.domain.Invoice;
import com.businesshub.dashboard.domain.InvoiceStatus;
import com.businesshub.dashboard.domain.Lead;
import com.businesshub.dashboard.domain.LeadSource;
import com.businesshub.dashboard.domain.LeadStatus;
import com.businesshub.dashboard.domain.NotificationType;
import com.businesshub.dashboard.repository.InvoiceRepository;
import com.businesshub.dashboard.repository.LeadRepository;
import com.businesshub.dashboard.repository.NotificationRepository;
import com.businesshub.dashboard.domain.AppNotification;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner loadSampleData(LeadRepository leadRepository,
                                     InvoiceRepository invoiceRepository,
                                     NotificationRepository notificationRepository) {
        return args -> {
            if (leadRepository.count() > 0) {
                return;
            }

            Lead lead1 = createLead("Nandi Mokoena", "nandi@lumastudio.co.za", "+27 82 555 0111",
                    "Luma Studio", LeadSource.WEBSITE, LeadStatus.NEW, "Requested a sales automation review.");
            Lead lead2 = createLead("Pieter Jacobs", "pieter@urbanfleet.co.za", "+27 83 555 0198",
                    "Urban Fleet", LeadSource.WHATSAPP, LeadStatus.CONTACTED, "Needs weekly reporting dashboards.");
            Lead lead3 = createLead("Aisha Daniels", "aisha@harbourretail.co.za", "+27 71 555 0105",
                    "Harbour Retail", LeadSource.CSV_IMPORT, LeadStatus.QUOTED, "Imported from expo lead sheet.");
            Lead lead4 = createLead("Thabo Ndlovu", "thabo@craftlogic.co.za", "+27 72 555 0140",
                    "Craft Logic", LeadSource.WEBHOOK, LeadStatus.WON, "Ready for onboarding and invoicing.");

            List<Lead> savedLeads = leadRepository.saveAll(List.of(lead1, lead2, lead3, lead4));

            invoiceRepository.save(createInvoice("INV-20260424-001", "Craft Logic", new BigDecimal("18500.00"),
                    LocalDate.now().plusDays(14), InvoiceStatus.PENDING, savedLeads.get(3)));
            invoiceRepository.save(createInvoice("INV-20260424-002", "Harbour Retail", new BigDecimal("7200.00"),
                    LocalDate.now().minusDays(3), InvoiceStatus.OVERDUE, savedLeads.get(2)));
            invoiceRepository.save(createInvoice("INV-20260424-003", "Urban Fleet", new BigDecimal("12800.00"),
                    LocalDate.now().minusDays(10), InvoiceStatus.PAID, savedLeads.get(1)));

            notificationRepository.saveAll(List.of(
                    createNotification(NotificationType.INFO, "New lead captured from WEBSITE: Nandi Mokoena"),
                    createNotification(NotificationType.WARNING, "Invoice INV-20260424-002 is overdue"),
                    createNotification(NotificationType.SUCCESS, "Lead Thabo Ndlovu moved to WON")
            ));
        };
    }

    private Lead createLead(String name, String email, String phone, String company,
                            LeadSource source, LeadStatus status, String notes) {
        Lead lead = new Lead();
        lead.setName(name);
        lead.setEmail(email);
        lead.setPhone(phone);
        lead.setCompany(company);
        lead.setSource(source);
        lead.setStatus(status);
        lead.setNotes(notes);
        return lead;
    }

    private Invoice createInvoice(String invoiceNumber, String clientName, BigDecimal amount,
                                  LocalDate dueDate, InvoiceStatus status, Lead lead) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setClientName(clientName);
        invoice.setAmount(amount);
        invoice.setDueDate(dueDate);
        invoice.setStatus(status);
        invoice.setLead(lead);
        return invoice;
    }

    private AppNotification createNotification(NotificationType type, String message) {
        AppNotification notification = new AppNotification();
        notification.setType(type);
        notification.setMessage(message);
        notification.setRead(false);
        return notification;
    }
}
