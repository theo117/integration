package com.businesshub.dashboard.web.api;

import com.businesshub.dashboard.config.SecurityConfig;
import com.businesshub.dashboard.domain.AppUser;
import com.businesshub.dashboard.domain.UserRole;
import com.businesshub.dashboard.service.AppUserService;
import com.businesshub.dashboard.service.DatabaseUserDetailsService;
import com.businesshub.dashboard.web.response.ApiResponseMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserManagementApiController.class)
@Import({ApiResponseMapper.class, ApiExceptionHandler.class, SecurityConfig.class})
class UserManagementApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppUserService appUserService;

    @MockBean
    private DatabaseUserDetailsService databaseUserDetailsService;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getUsersReturnsList() throws Exception {
        AppUser user = buildUser(1L, "ops", UserRole.OPS, true);
        when(appUserService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("ops"))
                .andExpect(jsonPath("$[0].role").value("OPS"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createUserReturnsCreatedUser() throws Exception {
        when(appUserService.createUser(any())).thenReturn(buildUser(2L, "ops-team", UserRole.OPS, true));

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ops-team",
                                  "password": "StrongPass1",
                                  "role": "OPS"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("ops-team"))
                .andExpect(jsonPath("$.role").value("OPS"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateUserStatusReturnsUpdatedUser() throws Exception {
        when(appUserService.updateUserStatus(eq(3L), eq(false), eq("admin")))
                .thenReturn(buildUser(3L, "ops-user", UserRole.OPS, false));

        mockMvc.perform(patch("/api/users/3/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "active": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ops-user"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void resetPasswordReturnsNoContent() throws Exception {
        mockMvc.perform(patch("/api/users/3/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "newPassword": "ResetPass123"
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "ops", roles = "OPS")
    void changeOwnPasswordReturnsSuccessMessage() throws Exception {
        mockMvc.perform(post("/api/users/account/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "CurrentPass1",
                                  "newPassword": "NewPass123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password updated successfully"));
    }

    private AppUser buildUser(Long id, String username, UserRole role, boolean active) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setActive(active);
        user.setCreatedAt(LocalDateTime.of(2026, 4, 24, 15, 0));
        return user;
    }
}
