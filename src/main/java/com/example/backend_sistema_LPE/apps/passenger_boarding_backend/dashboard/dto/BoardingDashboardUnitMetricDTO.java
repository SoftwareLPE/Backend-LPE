package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto;

public record BoardingDashboardUnitMetricDTO(
        Long unitId,
        String unitName,
        String routeCode,
        long value
) {
}
