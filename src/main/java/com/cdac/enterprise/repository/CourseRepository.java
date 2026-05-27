package com.cdac.enterprise.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.cdac.enterprise.entity.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByCodeAndDeletedFalse(String code);

    Optional<Course> findByIdAndDeletedFalse(Long id);

    Page<Course> findByDeletedFalse(Pageable pageable);

    Page<Course> findByActiveTrueAndDeletedFalse(Pageable pageable);

    Page<Course> findByNameContainingIgnoreCaseAndDeletedFalse(String name, Pageable pageable);

    Page<Course> findByNameContainingIgnoreCaseAndActiveTrueAndDeletedFalse(String name, Pageable pageable);

    long countByDeletedFalse();
}