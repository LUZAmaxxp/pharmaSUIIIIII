package com.pharmacy.admin.dto;

public record DashboardStatsDTO(
        long totalUsers,
        long totalPharmacies,
        long activePharmacies,
        long totalPrescriptions,
        long prescriptionsToday,
        double treatmentRatePercent
) {}
