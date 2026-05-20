package com.pharmacy.admin.service;

import com.pharmacy.admin.dto.PharmacySummaryDTO;
import com.pharmacy.admin.factory.NotificationFactory;
import com.pharmacy.admin.observer.AdminActionEvent;
import com.pharmacy.shared.entity.Pharmacy;
import com.pharmacy.shared.repository.PharmacyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminPharmacyService implements AdminPharmacyServiceI {

    private final PharmacyRepository pharmacyRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AdminPharmacyService(PharmacyRepository pharmacyRepository,
                                ApplicationEventPublisher eventPublisher) {
        this.pharmacyRepository = pharmacyRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PharmacySummaryDTO> getAllPharmacies(Pageable pageable) {
        return pharmacyRepository.findAll(pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacySummaryDTO getPharmacyById(Long id) {
        Pharmacy pharmacy = pharmacyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy with id " + id + " not found"));
        return toDto(pharmacy);
    }

    @Override
    public PharmacySummaryDTO updateStatus(Long id, boolean active, Long adminId) {
        Pharmacy pharmacy = pharmacyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy with id " + id + " not found"));

        pharmacy.setActive(active);
        Pharmacy saved = pharmacyRepository.save(pharmacy);

        // Observer: publish audit event (async listener persists AuditLog)
        eventPublisher.publishEvent(new AdminActionEvent(
                this,
                adminId,
                "TOGGLE_STATUS",
                "PHARMACY",
                saved.getId(),
                "active=" + active
        ));

        // Factory Method: send notification if email is present
        if (saved.getEmail() != null && !saved.getEmail().isBlank()) {
            NotificationFactory
                    .createNotification("EMAIL", saved.getEmail(),
                            "Your pharmacy status has been changed to: " + (active ? "ACTIVE" : "INACTIVE"))
                    .send();
        }

        return toDto(saved);
    }

    private PharmacySummaryDTO toDto(Pharmacy pharmacy) {
        return new PharmacySummaryDTO(
                pharmacy.getId(),
                pharmacy.getName(),
                pharmacy.getAddress(),
                pharmacy.isActive(),
                pharmacy.getLatitude(),
                pharmacy.getLongitude()
        );
    }
}
