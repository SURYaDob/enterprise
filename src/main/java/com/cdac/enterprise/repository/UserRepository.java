package com.cdac.enterprise.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cdac.enterprise.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByIdAndDeletedFalse(Long id);

    Optional<User> findByEmailAndDeletedFalse(String email);

    boolean existsByEmailAndDeletedFalse(String email);

    long countByDeletedFalse();

    org.springframework.data.domain.Page<User> findByDeletedFalse(org.springframework.data.domain.Pageable pageable);

    Optional<User> findByResetToken(String resetToken);
}