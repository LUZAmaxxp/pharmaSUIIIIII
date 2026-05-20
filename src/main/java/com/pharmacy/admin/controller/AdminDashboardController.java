package com.pharmacy.admin.controller;

import com.pharmacy.admin.dto.DashboardStatsDTO;
import com.pharmacy.admin.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administrator operations")
@SecurityRequirement(name = "bearerAuth")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get admin KPI dashboard statistics")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Dashboard statistics returned successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied — ADMIN role required")
    })
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(adminDashboardService.getStats());
    }
}
