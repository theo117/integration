package com.businesshub.dashboard.web.response;

import com.businesshub.dashboard.domain.UserRole;

import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String username;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;

    public UserResponse(Long id, String username, UserRole role, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
