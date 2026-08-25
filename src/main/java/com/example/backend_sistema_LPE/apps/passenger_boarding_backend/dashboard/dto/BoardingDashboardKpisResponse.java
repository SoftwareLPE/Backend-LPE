package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto;

public record BoardingDashboardKpisResponse(
        long totalBoardings,
        long activeUnits,
        long incidents,
        double compliancePercentage,
        double averageBoardingsPerUnit,
        double averageBoardingsPerShift,
        double averageBoardingsPerDay,
        BoardingDashboardUnitKpiDTO topUnit,
        BoardingDashboardUnitKpiDTO lowestUnit,
        BoardingDashboardRouteKpiDTO topRoute,
        double entryPercentage,
        double exitPercentage
) {
}
