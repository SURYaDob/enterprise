package com.cdac.enterprise.dto;

import java.time.LocalDateTime;

import com.cdac.enterprise.constant.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private Long userId;
    private NotificationType type;
    private String title;
    private String message;
    private Boolean readStatus;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}