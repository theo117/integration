package com.businesshub.dashboard.config;

import com.businesshub.dashboard.domain.AppUser;
import com.businesshub.dashboard.domain.UserRole;
import com.businesshub.dashboard.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityDataInitializer {

    @Bean
    CommandLineRunner loadSecurityUsers(AppUserRepository appUserRepository,
                                        SecurityProperties securityProperties,
                                        PasswordEncoder passwordEncoder) {
        return args -> {
            seedUserIfMissing(appUserRepository, passwordEncoder, securityProperties.getAdmin());
            seedUserIfMissing(appUserRepository, passwordEncoder, securityProperties.getOps());
        };
    }

    private void seedUserIfMissing(AppUserRepository appUserRepository,
                                   PasswordEncoder passwordEncoder,
                                   SecurityProperties.User userConfig) {
        appUserRepository.findByUsernameIgnoreCase(userConfig.getUsername())
                .orElseGet(() -> {
                    AppUser user = new AppUser();
                    user.setUsername(userConfig.getUsername());
                    user.setPasswordHash(passwordEncoder.encode(userConfig.getPassword()));
                    user.setRole(UserRole.valueOf(userConfig.getRole()));
                    user.setActive(true);
                    return appUserRepository.save(user);
                });
    }
}
