package com.cdac.enterprise.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cdac.enterprise.constant.ApiPaths;
import com.cdac.enterprise.constant.AppMessages;
import com.cdac.enterprise.dto.ApiResponse;
import com.cdac.enterprise.dto.UserResponse;
import com.cdac.enterprise.entity.User;
import com.cdac.enterprise.exception.ResourceNotFoundException;
import com.cdac.enterprise.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Tag(name = "Admin Users", description = "Admin APIs for managing users")
@RestController
@RequestMapping(ApiPaths.API_V1 + "/admin/users")
public class UserAdminController {

    private final UserRepository userRepository;

    public UserAdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Operation(
            summary = "Get all users",
            description = "Returns paginated list of all non-deleted users. Requires ROLE_ADMIN."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<User> usersPage = userRepository.findByDeletedFalse(pageable);

        Page<UserResponse> response = usersPage.map(user -> UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Users fetched successfully",
                        response
                ));
    }

    @Operation(
            summary = "Get user by ID",
            description = "Returns a user by their ID. Requires ROLE_ADMIN. Excludes soft-deleted users."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(AppMessages.USER_NOT_FOUND));

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        "User fetched successfully",
                        response
                ));
    }

    @Operation(
            summary = "Search user by email",
            description = "Search for a user by exact email. Requires ROLE_ADMIN."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<UserResponse>> searchUsersByEmail(
            @RequestParam @Email String email
    ) {
        User user = userRepository.findByEmailAndDeletedFalse(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(AppMessages.USER_NOT_FOUND));

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        "User fetched successfully",
                        response
                ));
    }

    @Operation(
            summary = "Delete user (soft)",
            description = "Soft deletes a user by ID. Requires ROLE_ADMIN. Excludes soft-deleted users."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(AppMessages.USER_NOT_FOUND));

        user.setDeleted(true);
        user.setEnabled(false);
        userRepository.save(user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        "User deleted successfully",
                        null
                ));
    }

    @Operation(
            summary = "Update user profile",
            description = "Updates user details. Requires ROLE_ADMIN. Excludes soft-deleted users."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(AppMessages.USER_NOT_FOUND));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        User savedUser = userRepository.save(user);

        UserResponse response = UserResponse.builder()
                .id(savedUser.getId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .phoneNumber(savedUser.getPhoneNumber())
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        "User updated successfully",
                        response
                ));
    }

    @Getter
    @Setter
    public static class UpdateUserRequest {
        private String firstName;
        private String lastName;
        private String phoneNumber;
    }
}
