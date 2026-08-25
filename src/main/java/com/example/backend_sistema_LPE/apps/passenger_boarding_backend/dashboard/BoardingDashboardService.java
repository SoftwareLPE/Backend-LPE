package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto.BoardingDashboardByShiftResponse;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto.BoardingDashboardByUnitResponse;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto.BoardingDashboardDistributionResponse;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto.BoardingDashboardKpisResponse;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto.BoardingDashboardTimeSeriesResponse;

public interface BoardingDashboardService {
    BoardingDashboardKpisResponse getKpis(
            Long plantId,
            Long from,
            Long to,
            Long resolvedShiftId,
            String windowType,
            Long unitId,
            String routeCode
    );

    BoardingDashboardByUnitResponse getBoardingsByUnit(
            Long plantId,
            Long from,
            Long to,
            Long resolvedShiftId,
            String windowType,
            Long unitId,
            String routeCode
    );

    BoardingDashboardByShiftResponse getBoardingsByShift(
            Long plantId,
            Long from,
            Long to,
            Long resolvedShiftId,
            String windowType,
            Long unitId,
            String routeCode
    );

    BoardingDashboardTimeSeriesResponse getTimeSeries(
            Long plantId,
            Long from,
            Long to,
            Long resolvedShiftId,
            String windowType,
            Long unitId,
            String routeCode,
            String groupBy
    );

    BoardingDashboardDistributionResponse getDistribution(
            Long plantId,
            Long from,
            Long to,
            Long resolvedShiftId,
            String windowType,
            Long unitId,
            String routeCode
    );
}
