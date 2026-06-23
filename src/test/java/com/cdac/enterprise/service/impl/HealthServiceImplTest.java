package com.cdac.enterprise.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cdac.enterprise.dto.HealthResponse;

class HealthServiceImplTest {

    private HealthServiceImpl healthService;

    @BeforeEach
    void setUp() {
        healthService = new HealthServiceImpl();
    }

    @Test
    void getHealthStatus_shouldReturnUp() {
        HealthResponse response = healthService.getHealthStatus();

        assertNotNull(response);
        assertEquals("CDAC Enterprise Application", response.getApplication());
        assertEquals("UP", response.getStatus());
    }
}
