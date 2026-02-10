package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.dto.CascadaInboxViewDTO;
import com.example.backend_sistema_LPE.dto.CascadaWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.DriverViewDTO;
import com.example.backend_sistema_LPE.dto.ShiftDTO;
import com.example.backend_sistema_LPE.service.CascadaService;
import com.example.backend_sistema_LPE.service.DriverService;
import com.example.backend_sistema_LPE.service.ShiftService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/cascadas/inbox-view")
public class CascadaInboxViewController {
    private final CascadaService cascadaService;
    private final ShiftService shiftService;
    private final DriverService driverService;

    public CascadaInboxViewController(
            CascadaService cascadaService,
            ShiftService shiftService,
            DriverService driverService
    ) {
        this.cascadaService = cascadaService;
        this.shiftService = shiftService;
        this.driverService = driverService;
    }

    @GetMapping
    public ResponseEntity<CascadaInboxViewDTO> getInboxView(
            @RequestParam Long plantId,
            @RequestParam("weekDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean activeDrivers
    ) {
        String effectiveStatus = (status == null || status.isBlank()) ? "SENT" : status;

        CascadaWeekResponseDTO weekResponse = cascadaService.getWeekCascadas(plantId, weekDate, effectiveStatus);
        List<ShiftDTO> shifts = shiftService.getShiftsByPlant(plantId);
        List<DriverViewDTO> drivers = driverService.getDriversByPlant(plantId, activeDrivers);

        CascadaInboxViewDTO response = new CascadaInboxViewDTO(
                plantId,
                weekDate,
                effectiveStatus,
                shifts,
                drivers,
                weekResponse.getItems()
        );

        return ResponseEntity.ok(response);
    }
}
