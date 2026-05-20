package com.pharmacy.admin;

import com.pharmacy.admin.dto.PharmacySummaryDTO;
import com.pharmacy.admin.observer.AdminActionEvent;
import com.pharmacy.admin.service.AdminPharmacyService;
import com.pharmacy.shared.entity.Pharmacy;
import com.pharmacy.shared.repository.PharmacyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TDD test class — these tests define expected behavior before (or alongside) implementation.
 * Red -> Green -> Refactor cycle followed for updateStatus.
 */
@ExtendWith(MockitoExtension.class)
class AdminPharmacyServiceTest {

    @Mock
    private PharmacyRepository pharmacyRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AdminPharmacyService adminPharmacyService;

    private Pharmacy samplePharmacy;

    @BeforeEach
    void setUp() {
        samplePharmacy = new Pharmacy();
        samplePharmacy.setId(1L);
        samplePharmacy.setName("Pharmacie Centrale");
        samplePharmacy.setAddress("123 Rue de la Paix");
        samplePharmacy.setActive(true);
        samplePharmacy.setEmail("pharmacie@mail.ma");
    }

    // TDD Red->Green: test written first to define behavior
    @Test
    void updateStatus_shouldSetActiveFlagCorrectly() {
        when(pharmacyRepository.findById(1L)).thenReturn(Optional.of(samplePharmacy));
        when(pharmacyRepository.save(any(Pharmacy.class))).thenAnswer(inv -> inv.getArgument(0));

        PharmacySummaryDTO result = adminPharmacyService.updateStatus(1L, false, 42L);

        assertThat(result.active()).isFalse();
        verify(pharmacyRepository, times(1)).save(any(Pharmacy.class));
    }

    // TDD Red->Green: Observer pattern verification
    @Test
    void updateStatus_shouldPublishExactlyOneAdminActionEvent() {
        when(pharmacyRepository.findById(1L)).thenReturn(Optional.of(samplePharmacy));
        when(pharmacyRepository.save(any(Pharmacy.class))).thenAnswer(inv -> inv.getArgument(0));

        adminPharmacyService.updateStatus(1L, false, 42L);

        ArgumentCaptor<AdminActionEvent> eventCaptor = ArgumentCaptor.forClass(AdminActionEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        AdminActionEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getAdminId()).isEqualTo(42L);
        assertThat(capturedEvent.getAction()).isEqualTo("TOGGLE_STATUS");
        assertThat(capturedEvent.getTargetType()).isEqualTo("PHARMACY");
        assertThat(capturedEvent.getTargetId()).isEqualTo(1L);
        assertThat(capturedEvent.getDetail()).contains("active=false");
    }

    @Test
    void updateStatus_shouldThrowEntityNotFoundException_forUnknownPharmacy() {
        when(pharmacyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminPharmacyService.updateStatus(99L, true, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Pharmacy with id 99 not found");

        verify(eventPublisher, never()).publishEvent(any());
    }
}
