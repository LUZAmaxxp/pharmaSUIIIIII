package com.pharmacy.patient.dto;

public record AuthResponseDTO(
        String token,
        String role
) {}