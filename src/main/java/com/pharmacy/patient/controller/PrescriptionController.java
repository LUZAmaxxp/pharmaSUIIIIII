package com.pharmacy.patient.controller;

import com.pharmacy.patient.dto.CreatePrescriptionRequest;

import com.pharmacy.shared.entity.Prescription;
import com.pharmacy.shared.entity.User;

import com.pharmacy.shared.repository.PrescriptionRepository;
import com.pharmacy.shared.repository.UserRepository;
import com.pharmacy.shared.security.AdminPrincipal;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import java.io.File;
import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1/patient/prescriptions")

public class PrescriptionController {

    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;

    public PrescriptionController(
            PrescriptionRepository prescriptionRepository,
            UserRepository userRepository
    ) {
        this.prescriptionRepository = prescriptionRepository;
        this.userRepository = userRepository;
    }

    @PostMapping

    public ResponseEntity<?> createPrescription(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {

        AdminPrincipal principal =
                (AdminPrincipal) authentication.getPrincipal();

        String email = principal.getEmail();

        User patient = userRepository.findByEmail(email)
                .orElseThrow();

        String uploadDir = "uploads/";

        File directory = new File(uploadDir);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName =
                System.currentTimeMillis()
                        + "_"
                        + file.getOriginalFilename();

        Path filePath =
                Paths.get(uploadDir, fileName);

        Files.write(
                filePath,
                file.getBytes()
        );

        Prescription prescription =
                new Prescription();

        prescription.setPatient(patient);

        prescription.setFileUrl(
                "/uploads/" + fileName
        );

        prescription.setStatus("PENDING");

        prescriptionRepository.save(prescription);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Prescription uploaded successfully",

                        "fileUrl",
                        prescription.getFileUrl()
                )
        );
    }
    @GetMapping
    public ResponseEntity<List<Prescription>> getMyPrescriptions(
            Authentication authentication
    ) {

        AdminPrincipal principal =
        (AdminPrincipal) authentication.getPrincipal();

        String email = principal.getEmail();

        User patient = userRepository.findByEmail(email)
                .orElseThrow();

        List<Prescription> prescriptions =
                prescriptionRepository.findByPatient(patient);

        return ResponseEntity.ok(prescriptions);
    }


    @GetMapping("/{id}")

    public ResponseEntity<?> getPrescriptionById(
            @PathVariable Long id,
            Authentication authentication
    ) {

        AdminPrincipal principal =
                (AdminPrincipal) authentication.getPrincipal();

        String email = principal.getEmail();

        User patient = userRepository.findByEmail(email)
                .orElseThrow();

        Prescription prescription =
                prescriptionRepository
                        .findByIdAndPatient(id, patient)
                        .orElseThrow();

        return ResponseEntity.ok(
                Map.of(
                        "id", prescription.getId(),
                        "status", prescription.getStatus(),
                        "fileUrl", prescription.getFileUrl(),
                        "createdAt", prescription.getCreatedAt()
                )
        );
    }
    @PatchMapping("/{id}/cancel")

    public ResponseEntity<?> cancelPrescription(
            @PathVariable Long id,
            Authentication authentication
    ) {

        AdminPrincipal principal =
                (AdminPrincipal) authentication.getPrincipal();

        String email = principal.getEmail();

        User patient = userRepository.findByEmail(email)
                .orElseThrow();

        Prescription prescription =
                prescriptionRepository
                        .findByIdAndPatient(id, patient)
                        .orElseThrow();

        if (!prescription.getStatus().equals("PENDING")) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "error",
                            "Only pending prescriptions can be cancelled"
                    )
            );
        }

        prescription.setStatus("CANCELLED");

        prescriptionRepository.save(prescription);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Prescription cancelled successfully"
                )
        );
    }
}