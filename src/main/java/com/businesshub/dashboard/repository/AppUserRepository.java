package com.businesshub.dashboard.repository;

import com.businesshub.dashboard.domain.AppUser;
import com.businesshub.dashboard.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsernameIgnoreCase(String username);
    long countByRole(UserRole role);
    long countByActiveTrue();
}
