package com.pharmacy.shared.repository;
import com.pharmacy.shared.entity.User;
import java.util.List;
import com.pharmacy.shared.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    long countByCreatedAtGreaterThanEqual(LocalDateTime from);

    List<Prescription> findByPatient(User patient);

    Optional<Prescription> findByIdAndPatient(
        Long id,
        User patient
);
}
