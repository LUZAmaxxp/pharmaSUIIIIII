package com.pharmacy.admin;

import com.pharmacy.admin.dto.DashboardStatsDTO;
import com.pharmacy.admin.service.AdminDashboardService;
import com.pharmacy.shared.repository.OrderRepository;
import com.pharmacy.shared.repository.PharmacyRepository;
import com.pharmacy.shared.repository.PrescriptionRepository;
import com.pharmacy.shared.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TDD test class — getStats() behavior defined here before (or alongside) implementation.
 */
@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PharmacyRepository pharmacyRepository;

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    // TDD Red->Green: verify treatmentRatePercent calculation
    @Test
    void getStats_shouldReturnCorrectTreatmentRatePercent() {
        when(userRepository.count()).thenReturn(100L);
        when(pharmacyRepository.count()).thenReturn(20L);
        when(pharmacyRepository.countByActiveTrue()).thenReturn(15L);
        when(prescriptionRepository.count()).thenReturn(200L);
        when(prescriptionRepository.countByCreatedAtGreaterThanEqual(any(LocalDateTime.class))).thenReturn(40L);

        DashboardStatsDTO stats = adminDashboardService.getStats();

        assertThat(stats.totalUsers()).isEqualTo(100L);
        assertThat(stats.totalPharmacies()).isEqualTo(20L);
        assertThat(stats.activePharmacies()).isEqualTo(15L);
        assertThat(stats.totalPrescriptions()).isEqualTo(200L);
        assertThat(stats.prescriptionsToday()).isEqualTo(40L);
        // 40 / 200 * 100 = 20.0
        assertThat(stats.treatmentRatePercent()).isCloseTo(20.0, within(0.01));
    }

    @Test
    void getStats_shouldReturnZeroTreatmentRate_whenTotalPrescriptionsIsZero() {
        when(userRepository.count()).thenReturn(50L);
        when(pharmacyRepository.count()).thenReturn(5L);
        when(pharmacyRepository.countByActiveTrue()).thenReturn(5L);
        when(prescriptionRepository.count()).thenReturn(0L);
        when(prescriptionRepository.countByCreatedAtGreaterThanEqual(any(LocalDateTime.class))).thenReturn(0L);

        DashboardStatsDTO stats = adminDashboardService.getStats();

        // Division by zero guard — must return 0.0 not throw
        assertThat(stats.treatmentRatePercent()).isEqualTo(0.0);
    }
}
