package com.businesshub.dashboard.service;

import com.businesshub.dashboard.domain.AppUser;
import com.businesshub.dashboard.domain.UserRole;
import com.businesshub.dashboard.repository.AppUserRepository;
import com.businesshub.dashboard.web.request.CreateUserRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AppUserService appUserService;

    @Test
    void createsUserWithEncodedPassword() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("ops-team");
        request.setPassword("StrongPass1");
        request.setRole(UserRole.OPS);

        when(appUserRepository.findByUsernameIgnoreCase("ops-team")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("StrongPass1")).thenReturn("encoded-password");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser created = appUserService.createUser(request);

        assertThat(created.getUsername()).isEqualTo("ops-team");
        assertThat(created.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(created.getRole()).isEqualTo(UserRole.OPS);
        assertThat(created.isActive()).isTrue();
    }

    @Test
    void preventsDuplicateUsernames() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("admin");
        request.setPassword("StrongPass1");
        request.setRole(UserRole.ADMIN);

        when(appUserRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(new AppUser()));

        assertThatThrownBy(() -> appUserService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void preventsSelfDeactivation() {
        AppUser appUser = new AppUser();
        appUser.setId(1L);
        appUser.setUsername("admin");
        appUser.setActive(true);

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(appUser));

        assertThatThrownBy(() -> appUserService.updateUserStatus(1L, false, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot deactivate your own account");
    }

    @Test
    void resetsUserPassword() {
        AppUser appUser = new AppUser();
        appUser.setId(2L);
        appUser.setUsername("ops");

        when(appUserRepository.findById(2L)).thenReturn(Optional.of(appUser));
        when(passwordEncoder.encode("NewPassword1")).thenReturn("encoded-new-password");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        appUserService.resetPassword(2L, "NewPassword1");

        assertThat(appUser.getPasswordHash()).isEqualTo("encoded-new-password");
    }

    @Test
    void changesOwnPasswordWhenCurrentPasswordMatches() {
        AppUser appUser = new AppUser();
        appUser.setUsername("ops");
        appUser.setPasswordHash("old-encoded");
        appUser.setActive(true);

        when(appUserRepository.findByUsernameIgnoreCase("ops")).thenReturn(Optional.of(appUser));
        when(passwordEncoder.matches("CurrentPass1", "old-encoded")).thenReturn(true);
        when(passwordEncoder.encode("NewPass123")).thenReturn("new-encoded");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        appUserService.changeOwnPassword("ops", "CurrentPass1", "NewPass123");

        assertThat(appUser.getPasswordHash()).isEqualTo("new-encoded");
    }

    @Test
    void rejectsOwnPasswordChangeWhenCurrentPasswordIsWrong() {
        AppUser appUser = new AppUser();
        appUser.setUsername("ops");
        appUser.setPasswordHash("old-encoded");
        appUser.setActive(true);

        when(appUserRepository.findByUsernameIgnoreCase("ops")).thenReturn(Optional.of(appUser));
        when(passwordEncoder.matches("WrongPass", "old-encoded")).thenReturn(false);

        assertThatThrownBy(() -> appUserService.changeOwnPassword("ops", "WrongPass", "NewPass123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Current password is incorrect");
    }
}
