package com.cdac.enterprise.service.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cdac.enterprise.constant.AppMessages;
import com.cdac.enterprise.constant.NotificationType;
import com.cdac.enterprise.dto.NotificationResponse;
import com.cdac.enterprise.entity.Notification;
import com.cdac.enterprise.entity.User;
import com.cdac.enterprise.exception.ResourceNotFoundException;
import com.cdac.enterprise.repository.NotificationRepository;
import com.cdac.enterprise.repository.UserRepository;
import com.cdac.enterprise.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            UserRepository userRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void createNotification(
            User user,
            NotificationType type,
            String title,
            String message
    ) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .build();

        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(
            String userEmail,
            Boolean unreadOnly,
            Pageable pageable
    ) {
        User user = getUserByEmail(userEmail);

        Page<Notification> notifications = Boolean.TRUE.equals(unreadOnly)
                ? notificationRepository.findByUserAndReadStatusFalseAndDeletedFalse(user, pageable)
                : notificationRepository.findByUserAndDeletedFalse(user, pageable);

        return notifications.map(this::mapToNotificationResponse);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(String userEmail, Long notificationId) {
        User user = getUserByEmail(userEmail);

        Notification notification = notificationRepository
                .findByIdAndUserAndDeletedFalse(notificationId, user)
                .orElseThrow(() -> new ResourceNotFoundException(AppMessages.NOTIFICATION_NOT_FOUND));

        notification.setReadStatus(true);
        notification.setReadAt(LocalDateTime.now());

        Notification savedNotification = notificationRepository.save(notification);

        return mapToNotificationResponse(savedNotification);
    }

    private User getUserByEmail(String userEmail) {
        return userRepository.findByEmailAndDeletedFalse(userEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(AppMessages.USER_NOT_FOUND));
    }

    private NotificationResponse mapToNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .readStatus(notification.getReadStatus())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}