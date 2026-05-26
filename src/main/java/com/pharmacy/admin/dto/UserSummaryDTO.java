package com.pharmacy.admin.dto;

import java.time.LocalDateTime;

public record UserSummaryDTO(
        Long id,
        String email,
        String role,
        LocalDateTime createdAt
) {}
