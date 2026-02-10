package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.dto.CascadaWeekResponseDTO;
import com.example.backend_sistema_LPE.service.CascadaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/cascadas/week")
public class CascadaWeekController {
    private final CascadaService cascadaService;

    public CascadaWeekController(CascadaService cascadaService) {
        this.cascadaService = cascadaService;
    }

    @GetMapping
    public ResponseEntity<CascadaWeekResponseDTO> getWeekCascadas(
            @RequestParam Long plantId,
            @RequestParam("weekDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(
                cascadaService.getWeekCascadas(plantId, weekStartDate, status)
        );
    }
}
