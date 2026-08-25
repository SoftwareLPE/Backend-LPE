package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto;

public record BoardingDashboardShiftMetricDTO(
        Long shiftId,
        String shiftName,
        long value,
        double percentage
) {
}
