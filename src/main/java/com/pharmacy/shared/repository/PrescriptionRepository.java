package com.pharmacy.shared.repository;

import com.pharmacy.shared.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    long countByCreatedAtGreaterThanEqual(LocalDateTime from);
}
