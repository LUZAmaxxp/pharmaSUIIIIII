package com.pharmacy.admin.service;

import com.pharmacy.admin.dto.PharmacySummaryDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Decorator pattern — wraps AdminPharmacyService with SLF4J entry/exit logging.
 * Registered as @Primary so this bean is injected everywhere AdminPharmacyServiceI is required.
 */
@Primary
@Service
public class LoggingAdminPharmacyService implements AdminPharmacyServiceI {

    private static final Logger log = LoggerFactory.getLogger(LoggingAdminPharmacyService.class);

    private final AdminPharmacyService delegate;

    public LoggingAdminPharmacyService(@Qualifier("adminPharmacyService") AdminPharmacyService delegate) {
        this.delegate = delegate;
    }

    @Override
    public Page<PharmacySummaryDTO> getAllPharmacies(Pageable pageable) {
        log.info("[ADMIN] getAllPharmacies called — page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<PharmacySummaryDTO> result = delegate.getAllPharmacies(pageable);
        log.info("[ADMIN] getAllPharmacies completed — totalElements={}", result.getTotalElements());
        return result;
    }

    @Override
    public PharmacySummaryDTO getPharmacyById(Long id) {
        log.info("[ADMIN] getPharmacyById called — id={}", id);
        PharmacySummaryDTO result = delegate.getPharmacyById(id);
        log.info("[ADMIN] getPharmacyById completed — pharmacy={}", result.name());
        return result;
    }

    @Override
    public PharmacySummaryDTO updateStatus(Long id, boolean active, Long adminId) {
        log.info("[ADMIN] updateStatus called — pharmacyId={} active={} by adminId={}", id, active, adminId);
        PharmacySummaryDTO result = delegate.updateStatus(id, active, adminId);
        log.info("[ADMIN] updateStatus completed — result active={}", result.active());
        return result;
    }
}
