package com.businesshub.dashboard.service;

import com.businesshub.dashboard.domain.Lead;
import com.businesshub.dashboard.domain.LeadSource;
import com.businesshub.dashboard.repository.LeadRepository;
import com.businesshub.dashboard.repository.NotificationRepository;
import com.businesshub.dashboard.web.response.LeadImportResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadCsvImportServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private IntegrationEventService integrationEventService;

    @Test
    void importCsvCreatesLeadsAndReportsSkippedRows() {
        NotificationService notificationService = new NotificationService(notificationRepository);
        LeadCsvImportService leadCsvImportService = new LeadCsvImportService(leadRepository, notificationService, integrationEventService);

        String csv = """
                name,email,phone,company,notes,source
                Nandi Mokoena,nandi@example.com,+27 82 555 0111,Luma Studio,Interested in reporting,CSV_IMPORT
                ,missing@example.com,+27 82 555 0112,No Name Co,Bad row,WEBSITE
                Aisha Daniels,aisha@example.com,+27 82 555 0113,Harbour Retail,Imported from expo,WEBSITE
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "leads.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LeadImportResponse response = leadCsvImportService.importCsv(file);

        assertThat(response.importedCount()).isEqualTo(2);
        assertThat(response.skippedCount()).isEqualTo(1);
        assertThat(response.errors()).hasSize(1);

        ArgumentCaptor<Lead> leadCaptor = ArgumentCaptor.forClass(Lead.class);
        verify(leadRepository, times(2)).save(leadCaptor.capture());
        assertThat(leadCaptor.getAllValues().getFirst().getSource()).isEqualTo(LeadSource.CSV_IMPORT);
        assertThat(leadCaptor.getAllValues().get(1).getSource()).isEqualTo(LeadSource.WEBSITE);
        verify(notificationRepository).save(any());
    }

    @Test
    void importCsvRejectsMissingRequiredHeaders() {
        NotificationService notificationService = new NotificationService(notificationRepository);
        LeadCsvImportService leadCsvImportService = new LeadCsvImportService(leadRepository, notificationService, integrationEventService);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "leads.csv",
                "text/csv",
                "name,email,company\nNandi,nandi@example.com,Luma Studio".getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> leadCsvImportService.importCsv(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing required CSV columns");
    }

    @Test
    void importCsvReportsMalformedQuotedRowsAsSkipped() {
        NotificationService notificationService = new NotificationService(notificationRepository);
        LeadCsvImportService leadCsvImportService = new LeadCsvImportService(leadRepository, notificationService, integrationEventService);

        String csv = """
                name,email,phone,company,notes
                Nandi,nandi@example.com,+27 82 555 0111,Luma Studio,"Missing the closing quote
                Aisha,aisha@example.com,+27 82 555 0113,Harbour Retail,Valid row
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "leads.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LeadImportResponse response = leadCsvImportService.importCsv(file);

        assertThat(response.importedCount()).isEqualTo(1);
        assertThat(response.skippedCount()).isEqualTo(1);
        assertThat(response.errors()).containsExactly("Row 2: Malformed CSV row: missing closing quote");
        verify(leadRepository).save(any(Lead.class));
        verify(notificationRepository).save(any());
    }
}
