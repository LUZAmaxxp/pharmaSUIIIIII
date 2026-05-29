package com.pharmacy.patient.dto;

public record LoginRequestDTO(
        String email,
        String password
) {}