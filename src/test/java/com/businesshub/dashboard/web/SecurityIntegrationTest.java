package com.businesshub.dashboard.web;

import com.businesshub.dashboard.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    void unauthenticatedUserIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "ops", roles = "OPS")
    void opsUserCannotAccessReportsPage() throws Exception {
        mockMvc.perform(get("/reports"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "ops", roles = "OPS")
    void opsUserDoesNotSeeAdminOnlyNavigationLinks() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/integrations\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("href=\"/reports\""))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("href=\"/admin/users\""))));
    }

    @Test
    @WithMockUser(username = "ops", roles = "OPS")
    void opsUserCanAccessIntegrationsPage() throws Exception {
        mockMvc.perform(get("/integrations"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminUserCanAccessIntegrationsPage() throws Exception {
        mockMvc.perform(get("/integrations"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminUserCanAccessReportsPage() throws Exception {
        mockMvc.perform(get("/reports"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void dashboardIncludesCsrfMetadataForApiRequests() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf_header\"")));
    }

    @Test
    @WithMockUser(username = "ops", roles = "OPS")
    void opsUserCannotAccessUsersPage() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminUserCanAccessUsersPage() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "ops", roles = "OPS")
    void opsUserCanAccessOwnSecurityPage() throws Exception {
        mockMvc.perform(get("/account/security"))
                .andExpect(status().isOk());
    }

    @Test
    void actuatorHealthIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void csvTemplateIsPublic() throws Exception {
        mockMvc.perform(get("/samples/lead-import-template.csv"))
                .andExpect(status().isOk())
                .andExpect(content().string("name,email,phone,company,notes,source\n"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void unsafeApiRequestsRequireCsrfToken() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void unsafeApiRequestsAcceptCsrfToken() throws Exception {
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bootstrapUsersAreSeededInDatabase() {
        org.assertj.core.api.Assertions.assertThat(appUserRepository.findByUsernameIgnoreCase("admin")).isPresent();
        org.assertj.core.api.Assertions.assertThat(appUserRepository.findByUsernameIgnoreCase("ops")).isPresent();
    }
}
