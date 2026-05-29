package com.pharmacy.patient.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Order(1)

public class PatientSecurityConfig {

    @Bean
    public SecurityFilterChain patientFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

            .securityMatcher(
                    "/api/v1/patient/**",
                    "/uploads/**"
            )

            .cors(cors -> {})

            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS
                    )
            )

            .authorizeHttpRequests(auth -> auth

                    .requestMatchers(
                            "/api/v1/patient/auth/**",
                            "/uploads/**"
                    ).permitAll()

                    .anyRequest().authenticated()
            );

        return http.build();
    }
}