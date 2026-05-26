package com.pharmacy.admin.dto;

public record PharmacySummaryDTO(
        Long id,
        String name,
        String address,
        boolean active,
        Double latitude,
        Double longitude
) {}
