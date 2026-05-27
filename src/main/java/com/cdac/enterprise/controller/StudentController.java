package com.cdac.enterprise.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cdac.enterprise.dto.ApiResponse;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/student")
public class StudentController {

	@PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<String>> getDashboard(Principal principal) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Student dashboard loaded successfully",
                        "Welcome " + principal.getName()
                ));
    }
}