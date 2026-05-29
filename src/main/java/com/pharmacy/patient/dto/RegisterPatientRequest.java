package com.pharmacy.patient.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterPatientRequest(

        @NotBlank
        String fullname,

        @Email
        @NotBlank
        String email,

        @NotBlank
        String password,

        @NotBlank
        String phone

) {}