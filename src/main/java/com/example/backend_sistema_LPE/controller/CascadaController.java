package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.dto.CascadaResponseDTO;
import com.example.backend_sistema_LPE.dto.CascadaSaveRequestDTO;
import com.example.backend_sistema_LPE.dto.CascadaSendRequestDTO;
import com.example.backend_sistema_LPE.dto.CascadaStatusUpdateRequestDTO;
import com.example.backend_sistema_LPE.service.CascadaService;
import com.example.backend_sistema_LPE.security.UserPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
            @RequestParam("weekDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
            @RequestParam String shiftId,
            @RequestParam String dayKey,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(
                cascadaService.getCascada(plantId, weekStartDate, shiftId, dayKey, status)
        );
    }

    @PutMapping
    public ResponseEntity<Void> saveCascada(@RequestBody CascadaSaveRequestDTO request) {
        cascadaService.saveCascada(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{plantId}/send")
    public ResponseEntity<Void> sendCascada(
            @PathVariable Long plantId,
            @RequestBody CascadaSendRequestDTO request,
            Authentication authentication
    ) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getUserId();
        }
        cascadaService.sendCascada(
                plantId,
                request.getWeekDate(),
                request.getShiftId(),
                request.getDayKey(),
                userId,
                request.getRecipientUserIds()
        );
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{plantId}")
    public ResponseEntity<Void> deleteCascada(
            @PathVariable Long plantId,
            @RequestParam("weekDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
            @RequestParam String shiftId,
            @RequestParam(required = false) String dayKey,
            Authentication authentication
    ) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getUserId();
        }
        cascadaService.deleteCascada(plantId, weekStartDate, shiftId, dayKey, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{plantId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long plantId,
            @RequestBody CascadaStatusUpdateRequestDTO request,
            Authentication authentication
    ) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getUserId();
        }
        cascadaService.updateCascadaStatus(
                plantId,
                request.getWeekDate(),
                request.getShiftId(),
                request.getDayKey(),
                request.getStatus(),
                userId,
                request.getRecipientUserIds()
        );
        return ResponseEntity.noContent().build();
    }
}
