package com.businesshub.dashboard.web.api;

import com.businesshub.dashboard.config.SecurityConfig;
import com.businesshub.dashboard.domain.Lead;
import com.businesshub.dashboard.domain.LeadSource;
import com.businesshub.dashboard.domain.LeadStatus;
import com.businesshub.dashboard.integration.CsvLeadAdapter;
import com.businesshub.dashboard.integration.WebhookLeadAdapter;
import com.businesshub.dashboard.integration.WebsiteLeadAdapter;
import com.businesshub.dashboard.service.DatabaseUserDetailsService;
import com.businesshub.dashboard.service.LeadService;
import com.businesshub.dashboard.web.response.LeadImportResponse;
import com.businesshub.dashboard.web.response.ApiResponseMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LeadApiController.class)
@Import({ApiResponseMapper.class, ApiExceptionHandler.class, SecurityConfig.class})
class LeadApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeadService leadService;

    @MockBean
    private WebsiteLeadAdapter websiteLeadAdapter;

    @MockBean
    private WebhookLeadAdapter webhookLeadAdapter;

    @MockBean
    private CsvLeadAdapter csvLeadAdapter;

    @MockBean
    private DatabaseUserDetailsService databaseUserDetailsService;

    @Test
    @WithMockUser(username = "ops", roles = "OPS")
    void createLeadReturnsMappedDto() throws Exception {
        Lead lead = new Lead();
        lead.setId(5L);
        lead.setName("Nandi Mokoena");
        lead.setEmail("nandi@example.com");
        lead.setPhone("+27 82 555 0111");
        lead.setCompany("Luma Studio");
        lead.setSource(LeadSource.WEBSITE);
        lead.setStatus(LeadStatus.NEW);
        lead.setNotes("Requested a dashboard walkthrough.");

        when(websiteLeadAdapter.capture(any())).thenReturn(lead);

        mockMvc.perform(post("/api/leads")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Nandi Mokoena",
                                  "email": "nandi@example.com",
                                  "phone": "+27 82 555 0111",
                                  "company": "Luma Studio",
                                  "source": "WEBSITE",
                                  "notes": "Requested a dashboard walkthrough."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.company").value("Luma Studio"))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.source").value("WEBSITE"));
    }

    @Test
    @WithMockUser(username = "ops", roles = "OPS")
    void importCsvReturnsImportSummary() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "leads.csv",
                "text/csv",
                """
                name,email,phone,company
                Nandi,nandi@example.com,+27 82 555 0111,Luma Studio
                """.getBytes()
        );

        when(csvLeadAdapter.importFile(any())).thenReturn(new LeadImportResponse(1, 0, java.util.List.of()));

        mockMvc.perform(multipart("/api/leads/import/csv").file(file).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedCount").value(1))
                .andExpect(jsonPath("$.skippedCount").value(0))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void webhookWithoutIntegrationKeyReturnsForbidden() throws Exception {
        when(webhookLeadAdapter.capture(any(), any())).thenThrow(new SecurityException("Invalid integration key."));

        mockMvc.perform(post("/api/leads/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Contact Name",
                                  "email": "contact@example.com",
                                  "phone": "+27000000000",
                                  "company": "Company Name",
                                  "source": "WEBHOOK"
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}
