package com.pharmacy.admin.service;

import com.pharmacy.admin.dto.PharmacySummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminPharmacyServiceI {

    Page<PharmacySummaryDTO> getAllPharmacies(Pageable pageable);

    PharmacySummaryDTO getPharmacyById(Long id);

    PharmacySummaryDTO updateStatus(Long id, boolean active, Long adminId);
}
