package com.cdac.enterprise.service;

import com.cdac.enterprise.dto.HealthResponse;

public interface HealthService {

    HealthResponse getHealthStatus();
}