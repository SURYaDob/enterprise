package com.cdac.enterprise.config;

import static org.mockito.Mockito.*;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

import com.cdac.enterprise.security.CustomAccessDeniedHandler;
import com.cdac.enterprise.security.CustomUserDetailsService;
import com.cdac.enterprise.security.JwtAuthenticationEntryPoint;
import com.cdac.enterprise.security.JwtAuthenticationFilter;
import com.cdac.enterprise.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Shared test configuration for @WebMvcTest controller tests.
 *
 * Provides mock beans for JWT security dependencies (satisfies SecurityConfig's
 * constructor requirements) AND a permissive SecurityFilterChain (@Order(0) so
 * it takes precedence) that allows all requests without authentication.
 *
 * This enables testing controller request/response logic including Principal
 * injection via `with(user())` without needing real JWT tokens.
 */
@TestConfiguration
public class TestSecurityConfig {

    /**
     * Permissive filter chain — matches before SecurityConfig's chain
     * and permits all requests so tests don't require real auth.
     */
    @Bean
    @Order(0)
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    // ── Mock beans for SecurityConfig dependencies ──

    @Bean
    @Primary
    public JwtService jwtService() {
        return mock(JwtService.class);
    }

    @Bean
    @Primary
    public CustomUserDetailsService customUserDetailsService() {
        return mock(CustomUserDetailsService.class);
    }

    @Bean
    @Primary
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService
    ) {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    @Bean
    @Primary
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return new JwtAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    @Primary
    public CustomAccessDeniedHandler customAccessDeniedHandler(ObjectMapper objectMapper) {
        return new CustomAccessDeniedHandler(objectMapper);
    }

    @Bean
    @Primary
    public AuthenticationManager authenticationManager() {
        return mock(AuthenticationManager.class);
    }
}
