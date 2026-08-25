package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto.BoardingDashboardByShiftResponse;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto.BoardingDashboardByUnitResponse;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto.BoardingDashboardDistributionResponse;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto.BoardingDashboardKpisResponse;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto.BoardingDashboardTimeSeriesResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/boarding-dashboard")
public class BoardingDashboardController {

    private final BoardingDashboardService boardingDashboardService;

    public BoardingDashboardController(BoardingDashboardService boardingDashboardService) {
        this.boardingDashboardService = boardingDashboardService;
    }

    @GetMapping("/kpis")
    public ResponseEntity<BoardingDashboardKpisResponse> getKpis(
            @RequestParam Long plantId,
            @RequestParam Long from,
            @RequestParam Long to,
            @RequestParam(required = false) Long resolvedShiftId,
            @RequestParam(required = false) String windowType,
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) String routeCode
    ) {
        return ResponseEntity.ok(boardingDashboardService.getKpis(
                plantId,
                from,
                to,
                resolvedShiftId,
                windowType,
                unitId,
                routeCode
        ));
    }

    @GetMapping("/by-unit")
    public ResponseEntity<BoardingDashboardByUnitResponse> getBoardingsByUnit(
            @RequestParam Long plantId,
            @RequestParam Long from,
            @RequestParam Long to,
            @RequestParam(required = false) Long resolvedShiftId,
            @RequestParam(required = false) String windowType,
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) String routeCode
    ) {
        return ResponseEntity.ok(boardingDashboardService.getBoardingsByUnit(
                plantId,
                from,
                to,
                resolvedShiftId,
                windowType,
                unitId,
                routeCode
        ));
    }

    @GetMapping("/by-shift")
    public ResponseEntity<BoardingDashboardByShiftResponse> getBoardingsByShift(
            @RequestParam Long plantId,
            @RequestParam Long from,
            @RequestParam Long to,
            @RequestParam(required = false) Long resolvedShiftId,
            @RequestParam(required = false) String windowType,
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) String routeCode
    ) {
        return ResponseEntity.ok(boardingDashboardService.getBoardingsByShift(
                plantId,
                from,
                to,
                resolvedShiftId,
                windowType,
                unitId,
                routeCode
        ));
    }

    @GetMapping("/timeseries")
    public ResponseEntity<BoardingDashboardTimeSeriesResponse> getTimeSeries(
            @RequestParam Long plantId,
            @RequestParam Long from,
            @RequestParam Long to,
            @RequestParam(required = false) Long resolvedShiftId,
            @RequestParam(required = false) String windowType,
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) String routeCode,
            @RequestParam(defaultValue = "DAY") String groupBy
    ) {
        return ResponseEntity.ok(boardingDashboardService.getTimeSeries(
                plantId,
                from,
                to,
                resolvedShiftId,
                windowType,
                unitId,
                routeCode,
                groupBy
        ));
    }

    @GetMapping("/distribution")
    public ResponseEntity<BoardingDashboardDistributionResponse> getDistribution(
            @RequestParam Long plantId,
            @RequestParam Long from,
            @RequestParam Long to,
            @RequestParam(required = false) Long resolvedShiftId,
            @RequestParam(required = false) String windowType,
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) String routeCode
    ) {
        return ResponseEntity.ok(boardingDashboardService.getDistribution(
                plantId,
                from,
                to,
                resolvedShiftId,
                windowType,
                unitId,
                routeCode
        ));
    }
}
