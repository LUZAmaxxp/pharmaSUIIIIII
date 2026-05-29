package com.pharmacy.patient.controller;

import com.pharmacy.patient.dto.RegisterPatientRequest;
import com.pharmacy.patient.entity.Patient;
import com.pharmacy.patient.repository.PatientRepository;

import com.pharmacy.shared.entity.User;
import com.pharmacy.shared.repository.UserRepository;
import com.pharmacy.shared.security.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/patient/auth")
@Tag(name = "Patient Auth", description = "Patient authentication endpoints")

public class PatientAuthController {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public PatientAuthController(
            UserRepository userRepository,
            PatientRepository patientRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new patient")

    public ResponseEntity<?> registerPatient(
            @RequestBody @Valid RegisterPatientRequest request
    ) {

        if (userRepository.findByEmail(request.email()).isPresent()) {

            return ResponseEntity.badRequest().body(
                    Map.of("error", "Email already exists")
            );
        }

        User user = new User();

        user.setEmail(request.email());

        user.setPassword(
                passwordEncoder.encode(request.password())
        );

        user.setRole("ROLE_PATIENT");

        User savedUser = userRepository.save(user);

        Patient patient = new Patient();

        patient.setFullname(request.fullname());
        patient.setPhone(request.phone());
        patient.setUser(savedUser);

        patientRepository.save(patient);

        String token = jwtUtil.generateToken(savedUser);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Patient registered successfully",
                        "token", token
                )
        );
    }
}