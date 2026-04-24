package com.businesshub.dashboard.service;

import com.businesshub.dashboard.domain.AppNotification;
import com.businesshub.dashboard.domain.NotificationType;
import com.businesshub.dashboard.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public AppNotification create(NotificationType type, String message) {
        AppNotification notification = new AppNotification();
        notification.setType(type);
        notification.setMessage(message);
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    public List<AppNotification> getRecentNotifications() {
        return notificationRepository.findTop10ByOrderByCreatedAtDesc();
    }

    public long unreadCount() {
        return notificationRepository.countByReadFalse();
    }
}
