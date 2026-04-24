package com.businesshub.dashboard.service;

import com.businesshub.dashboard.config.EmailAutomationProperties;
import com.businesshub.dashboard.domain.Invoice;
import com.businesshub.dashboard.domain.Lead;
import com.businesshub.dashboard.domain.LeadStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class DefaultEmailAutomationService implements EmailAutomationService {

    private static final Logger log = LoggerFactory.getLogger(DefaultEmailAutomationService.class);

    private final JavaMailSender javaMailSender;
    private final EmailAutomationProperties emailAutomationProperties;

    public DefaultEmailAutomationService(ObjectProvider<JavaMailSender> javaMailSenderProvider,
                                         EmailAutomationProperties emailAutomationProperties) {
        this.javaMailSender = javaMailSenderProvider.getIfAvailable();
        this.emailAutomationProperties = emailAutomationProperties;
    }

    @Override
    public void sendLeadReceivedEmail(Lead lead) {
        sendMessage(
                lead.getEmail(),
                "We received your enquiry",
                "Hi " + lead.getName() + ",\n\n"
                        + "Thanks for contacting Business Operations Hub. "
                        + "We have logged your enquiry for " + lead.getCompany() + " and our team will follow up soon.\n\n"
                        + "Source: " + lead.getSource() + "\n"
                        + "Status: " + lead.getStatus() + "\n\n"
                        + "Business Operations Hub"
        );
    }

    @Override
    public void sendLeadStatusChangedEmail(Lead lead, LeadStatus previousStatus) {
        sendMessage(
                emailAutomationProperties.getOpsInbox(),
                "Lead status updated: " + lead.getName(),
                "Lead " + lead.getName() + " (" + lead.getCompany() + ") moved from "
                        + previousStatus + " to " + lead.getStatus() + "."
        );
    }

    @Override
    public void sendInvoiceCreatedEmail(Invoice invoice) {
        sendMessage(
                emailAutomationProperties.getOpsInbox(),
                "Invoice created: " + invoice.getInvoiceNumber(),
                "Invoice " + invoice.getInvoiceNumber() + " was created for " + invoice.getClientName()
                        + " with amount " + invoice.getAmount() + " due on " + invoice.getDueDate() + "."
        );
    }

    @Override
    public void sendInvoicePaidEmail(Invoice invoice) {
        sendMessage(
                emailAutomationProperties.getOpsInbox(),
                "Invoice paid: " + invoice.getInvoiceNumber(),
                "Invoice " + invoice.getInvoiceNumber() + " for " + invoice.getClientName()
                        + " has been marked as PAID."
        );
    }

    @Override
    public void sendInvoiceOverdueEmail(Invoice invoice) {
        sendMessage(
                emailAutomationProperties.getOpsInbox(),
                "Invoice overdue: " + invoice.getInvoiceNumber(),
                "Invoice " + invoice.getInvoiceNumber() + " for " + invoice.getClientName()
                        + " is overdue and requires follow-up."
        );
    }

    private void sendMessage(String to, String subject, String body) {
        if (!emailAutomationProperties.isEnabled()) {
            log.info("Email automation disabled. Would send email to {} with subject '{}'", to, subject);
            return;
        }

        if (javaMailSender == null) {
            log.warn("Email automation enabled but no JavaMailSender is configured. Skipping email to {} with subject '{}'", to, subject);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailAutomationProperties.getFrom());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        javaMailSender.send(message);
    }
}
