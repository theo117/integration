package com.businesshub.dashboard.service;

import com.businesshub.dashboard.domain.AppUser;
import com.businesshub.dashboard.domain.UserRole;
import com.businesshub.dashboard.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseUserDetailsServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private DatabaseUserDetailsService databaseUserDetailsService;

    @Test
    void loadsActiveUserFromDatabase() {
        AppUser appUser = new AppUser();
        appUser.setUsername("admin");
        appUser.setPasswordHash("$2a$10$encoded");
        appUser.setRole(UserRole.ADMIN);
        appUser.setActive(true);

        when(appUserRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(appUser));

        var userDetails = databaseUserDetailsService.loadUserByUsername("admin");

        assertThat(userDetails.getUsername()).isEqualTo("admin");
        assertThat(userDetails.getPassword()).isEqualTo("$2a$10$encoded");
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void throwsWhenUserDoesNotExist() {
        when(appUserRepository.findByUsernameIgnoreCase("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> databaseUserDetailsService.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("missing");
    }
}
