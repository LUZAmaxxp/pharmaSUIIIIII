package com.pharmacy.admin.service;

import com.pharmacy.admin.dto.DashboardStatsDTO;
import com.pharmacy.shared.repository.OrderRepository;
import com.pharmacy.shared.repository.PharmacyRepository;
import com.pharmacy.shared.repository.PrescriptionRepository;
import com.pharmacy.shared.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Singleton — single instance managed by Spring IoC container.
 * Aggregates KPI data from all repositories for the admin dashboard.
 * Spring @Service guarantees a single shared instance; no manual singleton boilerplate needed.
 */
@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final OrderRepository orderRepository;

    public AdminDashboardService(UserRepository userRepository,
                                 PharmacyRepository pharmacyRepository,
                                 PrescriptionRepository prescriptionRepository,
                                 OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.orderRepository = orderRepository;
    }

    public DashboardStatsDTO getStats() {
        long totalUsers = userRepository.count();
        long totalPharmacies = pharmacyRepository.count();
        long activePharmacies = pharmacyRepository.countByActiveTrue();
        long totalPrescriptions = prescriptionRepository.count();

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        long prescriptionsToday = prescriptionRepository.countByCreatedAtGreaterThanEqual(startOfToday);

        double treatmentRatePercent = totalPrescriptions > 0
                ? (prescriptionsToday * 100.0) / totalPrescriptions
                : 0.0;

        return new DashboardStatsDTO(
                totalUsers,
                totalPharmacies,
                activePharmacies,
                totalPrescriptions,
                prescriptionsToday,
                treatmentRatePercent
        );
    }
}
