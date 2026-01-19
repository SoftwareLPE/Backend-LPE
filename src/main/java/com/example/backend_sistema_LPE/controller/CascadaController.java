package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.dto.CascadaResponseDTO;
import com.example.backend_sistema_LPE.dto.CascadaSaveRequestDTO;
import com.example.backend_sistema_LPE.service.CascadaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/cascada")
public class CascadaController {
    private final CascadaService cascadaService;

    public CascadaController(CascadaService cascadaService) {
        this.cascadaService = cascadaService;
    }

    @GetMapping
    public ResponseEntity<CascadaResponseDTO> getCascada(
            @RequestParam Long plantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
            @RequestParam String shiftId,
            @RequestParam String dayKey
    ) {
        return ResponseEntity.ok(
                cascadaService.getCascada(plantId, weekStartDate, shiftId, dayKey)
        );
    }

    @PutMapping
    public ResponseEntity<Void> saveCascada(@RequestBody CascadaSaveRequestDTO request) {
        cascadaService.saveCascada(request);
        return ResponseEntity.noContent().build();
    }
}
