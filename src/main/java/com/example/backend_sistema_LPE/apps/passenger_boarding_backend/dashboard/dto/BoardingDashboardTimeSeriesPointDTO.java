package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto;

public record BoardingDashboardTimeSeriesPointDTO(
        String label,
        long value,
        Long from,
        Long to
) {
}
