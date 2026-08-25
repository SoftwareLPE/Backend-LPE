package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto;

public record BoardingDashboardEventTypeMetricDTO(
        String type,
        String label,
        long value,
        double percentage
) {
}
