package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.api;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.api.dto.PlantSyncRequest;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.api.dto.PlantSidebarDTO;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.api.dto.UnitPassengerRowDTO;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.api.dto.UnitSummaryDTO;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report.dto.ExecuteReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PassengerMonitoringController {

    private final PassengerMonitoringService passengerMonitoringService;
    private final PlantSyncService plantSyncService;

    public PassengerMonitoringController(
            PassengerMonitoringService passengerMonitoringService,
            PlantSyncService plantSyncService
    ) {
        this.passengerMonitoringService = passengerMonitoringService;
        this.plantSyncService = plantSyncService;
    }

    @GetMapping("/plants")
    public ResponseEntity<List<PlantSidebarDTO>> getPlants() {
        return ResponseEntity.ok(passengerMonitoringService.getPlantsSidebar());
    }

    @GetMapping("/plants/{plantId}/units")
    public ResponseEntity<List<UnitSummaryDTO>> getUnitsByPlant(
            @PathVariable Long plantId,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to
    ) {
        return ResponseEntity.ok(passengerMonitoringService.getUnitsByPlant(plantId, from, to));
    }

    @GetMapping("/plants/{plantId}/units/with-events")
    public ResponseEntity<List<UnitSummaryDTO>> getUnitsByPlantWithEvents(
            @PathVariable Long plantId,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to
    ) {
        return ResponseEntity.ok(passengerMonitoringService.getUnitsByPlantWithEvents(plantId, from, to));
    }

    @PostMapping("/plants/{plantId}/sync")
    public ResponseEntity<ExecuteReportResponse> syncPlant(
            @PathVariable Long plantId,
            @RequestBody(required = false) PlantSyncRequest request
    ) {
        return ResponseEntity.ok(plantSyncService.syncPlant(plantId, request));
    }

    @GetMapping("/units/{unitId}/passengers")
    public ResponseEntity<Page<UnitPassengerRowDTO>> getPassengersByUnit(
            @PathVariable Long unitId,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to,
            @RequestParam(required = false) String shift,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ResponseEntity.ok(passengerMonitoringService.getUnitPassengers(unitId, from, to, shift, q, page, size));
    }
}
