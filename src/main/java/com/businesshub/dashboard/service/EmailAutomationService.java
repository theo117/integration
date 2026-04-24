package com.businesshub.dashboard.service;

import com.businesshub.dashboard.domain.Invoice;
import com.businesshub.dashboard.domain.Lead;
import com.businesshub.dashboard.domain.LeadStatus;

public interface EmailAutomationService {
    void sendLeadReceivedEmail(Lead lead);
    void sendLeadStatusChangedEmail(Lead lead, LeadStatus previousStatus);
    void sendInvoiceCreatedEmail(Invoice invoice);
    void sendInvoicePaidEmail(Invoice invoice);
    void sendInvoiceOverdueEmail(Invoice invoice);
}
