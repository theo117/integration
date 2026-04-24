package com.businesshub.dashboard.service;

import com.businesshub.dashboard.config.EmailAutomationProperties;
import com.businesshub.dashboard.domain.Invoice;
import com.businesshub.dashboard.domain.Lead;
import com.businesshub.dashboard.domain.LeadStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DefaultEmailAutomationServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private ObjectProvider<JavaMailSender> javaMailSenderProvider;

    @Test
    void doesNotSendWhenEmailAutomationDisabled() {
        EmailAutomationProperties properties = new EmailAutomationProperties();
        properties.setEnabled(false);
        org.mockito.Mockito.when(javaMailSenderProvider.getIfAvailable()).thenReturn(javaMailSender);
        DefaultEmailAutomationService service = new DefaultEmailAutomationService(javaMailSenderProvider, properties);

        Lead lead = new Lead();
        lead.setName("Nandi");
        lead.setEmail("nandi@example.com");
        lead.setCompany("Luma");
        lead.setStatus(LeadStatus.NEW);
        lead.setSource(com.businesshub.dashboard.domain.LeadSource.WEBSITE);

        service.sendLeadReceivedEmail(lead);

        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendsEmailWhenAutomationEnabled() {
        EmailAutomationProperties properties = new EmailAutomationProperties();
        properties.setEnabled(true);
        properties.setFrom("ops@example.com");
        properties.setOpsInbox("ops@example.com");
        org.mockito.Mockito.when(javaMailSenderProvider.getIfAvailable()).thenReturn(javaMailSender);
        DefaultEmailAutomationService service = new DefaultEmailAutomationService(javaMailSenderProvider, properties);

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-20260424-001");
        invoice.setClientName("Craft Logic");
        invoice.setAmount(new BigDecimal("18500.00"));
        invoice.setDueDate(LocalDate.now().plusDays(10));

        service.sendInvoiceCreatedEmail(invoice);

        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(mailCaptor.capture());
        SimpleMailMessage message = mailCaptor.getValue();

        assertThat(message.getTo()).containsExactly("ops@example.com");
        assertThat(message.getFrom()).isEqualTo("ops@example.com");
        assertThat(message.getSubject()).contains("Invoice created");
        assertThat(message.getText()).contains("Craft Logic");
    }
}
