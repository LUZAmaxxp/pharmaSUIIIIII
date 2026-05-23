package com.pharmacy.admin.controller;

import com.pharmacy.admin.dto.PharmacySummaryDTO;
import com.pharmacy.admin.dto.PharmacyStatusUpdateDTO;
import com.pharmacy.admin.service.AdminPharmacyServiceI;
import com.pharmacy.shared.security.AdminPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administrator operations")
@SecurityRequirement(name = "bearerAuth")
public class AdminPharmacyController {

    private final AdminPharmacyServiceI adminPharmacyService;

    public AdminPharmacyController(AdminPharmacyServiceI adminPharmacyService) {
        this.adminPharmacyService = adminPharmacyService;
    }

    @GetMapping("/pharmacies")
    @Operation(summary = "Get all pharmacies (paginated)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Paginated list of pharmacies"),
        @ApiResponse(responseCode = "403", description = "Access denied — ADMIN role required")
    })
    public ResponseEntity<Page<PharmacySummaryDTO>> getAllPharmacies(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(adminPharmacyService.getAllPharmacies(pageable));
    }

    @GetMapping("/pharmacies/{id}")
    @Operation(summary = "Get pharmacy by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pharmacy found"),
        @ApiResponse(responseCode = "404", description = "Pharmacy not found"),
        @ApiResponse(responseCode = "403", description = "Access denied — ADMIN role required")
    })
    public ResponseEntity<PharmacySummaryDTO> getPharmacyById(@PathVariable Long id) {
        return ResponseEntity.ok(adminPharmacyService.getPharmacyById(id));
    }

    @PatchMapping("/pharmacies/{id}/status")
    @Operation(summary = "Toggle pharmacy active/inactive status")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error — active field is required"),
        @ApiResponse(responseCode = "404", description = "Pharmacy not found"),
        @ApiResponse(responseCode = "403", description = "Access denied — ADMIN role required")
    })
    public ResponseEntity<PharmacySummaryDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody @Valid PharmacyStatusUpdateDTO dto,
            @AuthenticationPrincipal AdminPrincipal principal) {
        Long adminId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(
                adminPharmacyService.updateStatus(id, dto.getActive(), adminId));
    }
}
