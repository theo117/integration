package com.businesshub.dashboard.service;

import com.businesshub.dashboard.domain.Lead;
import com.businesshub.dashboard.domain.LeadSource;
import com.businesshub.dashboard.domain.LeadStatus;
import com.businesshub.dashboard.repository.LeadRepository;
import com.businesshub.dashboard.repository.NotificationRepository;
import com.businesshub.dashboard.web.request.CreateLeadRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailAutomationService emailAutomationService;

    @Test
    void createLeadDefaultsSourceAndStatusAndCreatesNotification() {
        NotificationService notificationService = new NotificationService(notificationRepository);
        LeadService leadService = new LeadService(leadRepository, notificationService, emailAutomationService);

        CreateLeadRequest request = new CreateLeadRequest();
        request.setName("Nandi Mokoena");
        request.setEmail("nandi@example.com");
        request.setPhone("+27 82 555 0111");
        request.setCompany("Luma Studio");
        request.setNotes("Requested a dashboard demo.");

        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> {
            Lead lead = invocation.getArgument(0);
            lead.setId(1L);
            return lead;
        });
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Lead savedLead = leadService.createLead(request);

        ArgumentCaptor<Lead> leadCaptor = ArgumentCaptor.forClass(Lead.class);
        verify(leadRepository).save(leadCaptor.capture());
        Lead captured = leadCaptor.getValue();

        assertThat(captured.getSource()).isEqualTo(LeadSource.MANUAL_ENTRY);
        assertThat(captured.getStatus()).isEqualTo(LeadStatus.NEW);
        assertThat(savedLead.getId()).isEqualTo(1L);
        verify(notificationRepository, times(1)).save(any());
        verify(emailAutomationService, times(1)).sendLeadReceivedEmail(savedLead);
    }
}
