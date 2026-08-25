package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto;

public record BoardingDashboardRouteKpiDTO(
        String routeCode,
        String routeName,
        long boardings
) {
}
