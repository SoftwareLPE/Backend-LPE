package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto;

import java.util.List;

public record BoardingDashboardByUnitResponse(
        List<BoardingDashboardUnitMetricDTO> items
) {
}
