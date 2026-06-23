package com.cdac.enterprise.config;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.cdac.enterprise.constant.RoleName;
import com.cdac.enterprise.entity.Role;
import com.cdac.enterprise.entity.User;
import com.cdac.enterprise.repository.RoleRepository;
import com.cdac.enterprise.repository.UserRepository;

/**
 * Seeds essential data (roles and admin user) in all environments.
 * Runs before DataInitializer and is not profile-restricted.
 */
@Component
@Order(1)
public class EssentialDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(EssentialDataInitializer.class);

    private static final String DEFAULT_ADMIN_EMAIL = "admin@cdac.local";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@123";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public EssentialDataInitializer(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting essential data initialization...");

        // 1. Create roles
        Role studentRole = createRoleIfNotExists(RoleName.ROLE_STUDENT,
                "Default role for student users who can apply for courses");
        Role adminRole = createRoleIfNotExists(RoleName.ROLE_ADMIN,
                "Administrative role for managing users, courses, and applications");

        // 2. Create admin user
        createAdminUserIfNotExists(adminRole);

        log.info("Essential data initialization completed successfully.");
    }

    private Role createRoleIfNotExists(RoleName roleName, String description) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role role = Role.builder()
                            .name(roleName)
                            .description(description)
                            .build();
                    Role savedRole = roleRepository.save(role);
                    log.info("Seeded role: {}", roleName);
                    return savedRole;
                });
    }

    private User createAdminUserIfNotExists(Role adminRole) {
        return userRepository.findByEmailAndDeletedFalse(DEFAULT_ADMIN_EMAIL)
                .orElseGet(() -> {
                    User adminUser = User.builder()
                            .firstName("System")
                            .lastName("Admin")
                            .email(DEFAULT_ADMIN_EMAIL)
                            .password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                            .phoneNumber("9999999999")
                            .enabled(true)
                            .emailVerified(true)
                            .accountNonLocked(true)
                            .deleted(false)
                            .roles(Set.of(adminRole))
                            .build();
                    User saved = userRepository.save(adminUser);
                    log.info("Seeded admin user: {}", DEFAULT_ADMIN_EMAIL);
                    return saved;
                });
    }
}
