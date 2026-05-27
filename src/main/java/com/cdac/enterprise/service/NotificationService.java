package com.cdac.enterprise.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.cdac.enterprise.constant.NotificationType;
import com.cdac.enterprise.dto.NotificationResponse;
import com.cdac.enterprise.entity.User;

public interface NotificationService {

    void createNotification(
            User user,
            NotificationType type,
            String title,
            String message
    );

    Page<NotificationResponse> getMyNotifications(String userEmail, Boolean unreadOnly, Pageable pageable);

    NotificationResponse markAsRead(String userEmail, Long notificationId);
}