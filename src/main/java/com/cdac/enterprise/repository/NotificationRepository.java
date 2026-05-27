package com.cdac.enterprise.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.cdac.enterprise.entity.Notification;
import com.cdac.enterprise.entity.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserAndDeletedFalse(User user, Pageable pageable);

    Page<Notification> findByUserAndReadStatusFalseAndDeletedFalse(User user, Pageable pageable);

    Optional<Notification> findByIdAndUserAndDeletedFalse(Long id, User user);
}