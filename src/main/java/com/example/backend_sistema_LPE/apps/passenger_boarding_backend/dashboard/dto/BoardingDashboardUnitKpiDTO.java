package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto;

public record BoardingDashboardUnitKpiDTO(
        Long unitId,
        String unitName,
        long boardings
) {
}
