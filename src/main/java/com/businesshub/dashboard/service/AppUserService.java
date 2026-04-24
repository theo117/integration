package com.businesshub.dashboard.service;

import com.businesshub.dashboard.domain.AppUser;
import com.businesshub.dashboard.repository.AppUserRepository;
import com.businesshub.dashboard.web.request.CreateUserRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AppUser> getAllUsers() {
        return appUserRepository.findAll()
                .stream()
                .sorted((left, right) -> left.getUsername().compareToIgnoreCase(right.getUsername()))
                .toList();
    }

    public long getActiveUserCount() {
        return appUserRepository.countByActiveTrue();
    }

    @Transactional
    public AppUser createUser(CreateUserRequest request) {
        String normalizedUsername = request.getUsername().trim();
        if (appUserRepository.findByUsernameIgnoreCase(normalizedUsername).isPresent()) {
            throw new IllegalArgumentException("A user with that username already exists");
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(normalizedUsername);
        appUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        appUser.setRole(request.getRole());
        appUser.setActive(true);
        return appUserRepository.save(appUser);
    }

    @Transactional
    public AppUser updateUserStatus(Long userId, boolean active, String actingUsername) {
        AppUser appUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (!active && appUser.getUsername().equalsIgnoreCase(actingUsername)) {
            throw new IllegalArgumentException("You cannot deactivate your own account");
        }

        appUser.setActive(active);
        return appUserRepository.save(appUser);
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        AppUser appUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        appUser.setPasswordHash(passwordEncoder.encode(newPassword));
        appUserRepository.save(appUser);
    }

    @Transactional
    public void changeOwnPassword(String username, String currentPassword, String newPassword) {
        AppUser appUser = appUserRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        if (!appUser.isActive()) {
            throw new IllegalArgumentException("Your account is inactive");
        }

        if (!passwordEncoder.matches(currentPassword, appUser.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        appUser.setPasswordHash(passwordEncoder.encode(newPassword));
        appUserRepository.save(appUser);
    }
}
