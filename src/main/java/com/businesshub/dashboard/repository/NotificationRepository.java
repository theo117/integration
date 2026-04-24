package com.businesshub.dashboard.repository;

import com.businesshub.dashboard.domain.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<AppNotification, Long> {
    List<AppNotification> findTop10ByOrderByCreatedAtDesc();
    long countByReadFalse();
}
