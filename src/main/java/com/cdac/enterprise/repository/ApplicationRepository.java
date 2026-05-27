package com.cdac.enterprise.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.cdac.enterprise.constant.ApplicationStatus;
import com.cdac.enterprise.entity.Application;
import com.cdac.enterprise.entity.Course;
import com.cdac.enterprise.entity.User;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByUserAndCourseAndDeletedFalse(User user, Course course);

    Optional<Application> findByIdAndDeletedFalse(Long id);

    Optional<Application> findByIdAndUserAndDeletedFalse(Long id, User user);

    Page<Application> findByUserAndDeletedFalse(User user, Pageable pageable);

    Page<Application> findByDeletedFalse(Pageable pageable);

    Page<Application> findByStatusAndDeletedFalse(ApplicationStatus status, Pageable pageable);

    long countByDeletedFalse();

    long countByStatusAndDeletedFalse(ApplicationStatus status);
}