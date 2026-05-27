package com.cdac.enterprise.controller;

import java.security.Principal;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cdac.enterprise.constant.ApiPaths;
import com.cdac.enterprise.constant.AppMessages;
import com.cdac.enterprise.dto.ApiResponse;
import com.cdac.enterprise.dto.NotificationResponse;
import com.cdac.enterprise.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Notifications", description = "User APIs for viewing and reading notifications")
@RestController
@RequestMapping(ApiPaths.NOTIFICATIONS)
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(
            summary = "Get my notifications",
            description = "Returns notifications for the logged-in user. Supports unreadOnly filter."
    )
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getMyNotifications(
            Principal principal,
            @RequestParam(defaultValue = "false") Boolean unreadOnly,
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<NotificationResponse> notifications = notificationService.getMyNotifications(
                principal.getName(),
                unreadOnly,
                pageable
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        AppMessages.NOTIFICATIONS_FETCHED_SUCCESSFULLY,
                        notifications
                ));
    }

    @Operation(
            summary = "Mark notification as read",
            description = "Marks a notification as read if it belongs to the logged-in user."
    )
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            Principal principal,
            @PathVariable Long id
    ) {
        NotificationResponse notification = notificationService.markAsRead(
                principal.getName(),
                id
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        AppMessages.NOTIFICATION_MARKED_AS_READ,
                        notification
                ));
    }
}