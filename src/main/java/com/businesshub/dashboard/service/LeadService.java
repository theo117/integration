package com.businesshub.dashboard.service;

import com.businesshub.dashboard.domain.Lead;
import com.businesshub.dashboard.domain.LeadSource;
import com.businesshub.dashboard.domain.LeadStatus;
import com.businesshub.dashboard.domain.NotificationType;
import com.businesshub.dashboard.repository.LeadRepository;
import com.businesshub.dashboard.web.request.CreateLeadRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LeadService {

    private final LeadRepository leadRepository;
    private final NotificationService notificationService;
    private final EmailAutomationService emailAutomationService;

    public LeadService(LeadRepository leadRepository,
                       NotificationService notificationService,
                       EmailAutomationService emailAutomationService) {
        this.leadRepository = leadRepository;
        this.notificationService = notificationService;
        this.emailAutomationService = emailAutomationService;
    }

    public List<Lead> getAllLeads() {
        return leadRepository.findAll();
    }

    public List<Lead> getRecentLeads() {
        return leadRepository.findTop8ByOrderByCreatedAtDesc();
    }

    @Transactional
    public Lead createLead(CreateLeadRequest request) {
        Lead lead = new Lead();
        lead.setName(request.getName());
        lead.setEmail(request.getEmail());
        lead.setPhone(request.getPhone());
        lead.setCompany(request.getCompany());
        lead.setSource(request.getSource() == null ? LeadSource.MANUAL_ENTRY : request.getSource());
        lead.setStatus(LeadStatus.NEW);
        lead.setNotes(request.getNotes());

        Lead savedLead = leadRepository.save(lead);
        notificationService.create(NotificationType.INFO,
                "New lead received from " + savedLead.getSource() + ": " + savedLead.getName());
        emailAutomationService.sendLeadReceivedEmail(savedLead);
        return savedLead;
    }

    @Transactional
    public Lead updateStatus(Long leadId, LeadStatus status) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + leadId));
        LeadStatus previousStatus = lead.getStatus();
        lead.setStatus(status);
        Lead updatedLead = leadRepository.save(lead);

        NotificationType type = status == LeadStatus.WON ? NotificationType.SUCCESS : NotificationType.INFO;
        notificationService.create(type,
                "Lead " + updatedLead.getName() + " moved to " + status);
        emailAutomationService.sendLeadStatusChangedEmail(updatedLead, previousStatus);
        return updatedLead;
    }
}
