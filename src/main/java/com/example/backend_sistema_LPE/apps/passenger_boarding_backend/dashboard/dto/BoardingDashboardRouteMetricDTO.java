package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto;

public record BoardingDashboardRouteMetricDTO(
        String routeCode,
        String routeName,
        long value,
        double percentage
) {
}
