package com.pharmacy.shared.repository;

import com.pharmacy.shared.entity.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {
    long countByActiveTrue();
}
