package com.cdac.enterprise.service.impl;

import org.springframework.stereotype.Service;

import com.cdac.enterprise.dto.HealthResponse;
import com.cdac.enterprise.service.HealthService;

@Service
public class HealthServiceImpl implements HealthService {

    @Override
    public HealthResponse getHealthStatus() {
        return HealthResponse.builder()
                .application("CDAC Enterprise Application")
                .status("UP")
                .build();
    }
}