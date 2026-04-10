package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.dto.CascadaResponseDTO;
import com.example.backend_sistema_LPE.dto.CascadaSaveRequestDTO;
import com.example.backend_sistema_LPE.dto.CascadaStandardManualRowDTO;
import com.example.backend_sistema_LPE.dto.CascadaStatusUpdateRequestDTO;
import com.example.backend_sistema_LPE.dto.CascadaSummaryDTO;
import com.example.backend_sistema_LPE.dto.CascadaWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.CreateCascadaStandardManualRowRequestDTO;
import com.example.backend_sistema_LPE.dto.StandardWeeklyResponseDTO;
import com.example.backend_sistema_LPE.dto.UpdateCascadaStandardManualRowRequestDTO;
import com.example.backend_sistema_LPE.security.UserPrincipal;
import com.example.backend_sistema_LPE.service.CascadaStandardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/cascada/standard")
public class CascadaStandardController {
    private final CascadaStandardService cascadaStandardService;

    public CascadaStandardController(CascadaStandardService cascadaStandardService) {
        this.cascadaStandardService = cascadaStandardService;
    }

    @GetMapping
    public ResponseEntity<CascadaResponseDTO> getCascada(
            @RequestParam Long plantId,
            @RequestParam("weekDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekDate,
            @RequestParam String shiftId,
            @RequestParam(required = false) String dayKey,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok( cascadaStandardService.getCascada(plantId, weekDate, shiftId, dayKey, status) );

    }

    @PutMapping
    public ResponseEntity<Void> saveCascada(@RequestBody CascadaSaveRequestDTO request) {
        cascadaStandardService.saveCascada(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/manual-rows")
    public ResponseEntity<CascadaStandardManualRowDTO> createManualRow(
            @RequestBody CreateCascadaStandardManualRowRequestDTO request,
            Authentication authentication
    ) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getUserId();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cascadaStandardService.createManualRow(request, userId));
    }

    @PatchMapping("/manual-rows/{manualStandardRowId}")
    public ResponseEntity<CascadaStandardManualRowDTO> updateManualRow(
            @PathVariable Long manualStandardRowId,
            @RequestBody UpdateCascadaStandardManualRowRequestDTO request,
            Authentication authentication
    ) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getUserId();
        }
        return ResponseEntity.ok(cascadaStandardService.updateManualRow(manualStandardRowId, request, userId));
    }

    @DeleteMapping("/manual-rows/{manualStandardRowId}")
    public ResponseEntity<Void> deleteManualRow(@PathVariable Long manualStandardRowId) {
        cascadaStandardService.deleteManualRow(manualStandardRowId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{plantId}/status")
    public ResponseEntity<Void> updateCascadaStatus(
            @PathVariable Long plantId,
            @RequestBody CascadaStatusUpdateRequestDTO request,
            Authentication authentication
    ) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getUserId();
        }
        cascadaStandardService.updateCascadaStatus(
                plantId,
                request.getWeekDate(),
                request.getWeekStartDate(),
                request.getWeekEndDate(),
                request.getWeekNumber(),
                request.getShiftId(),
                request.getDayKey(),
                request.getStatus(),
                userId,
                request.getRecipientUserIds()
        );
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{plantId}")
    public ResponseEntity<Void> deleteCascada(
            @PathVariable Long plantId,
            @RequestParam("weekDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekDate,
            @RequestParam String shiftId,
            @RequestParam(required = false) String dayKey,
            Authentication authentication
    ) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getUserId();
        }
        cascadaStandardService.updateCascadaStatus(
                plantId,
                weekDate,
                null,
                null,
                null,
                shiftId,
                dayKey,
                "DELETED",
                userId,
                null
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/week")
    public ResponseEntity<CascadaWeekResponseDTO> getWeekCascadas(
            @RequestParam Long plantId,
            @RequestParam("weekDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekDate,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(
                cascadaStandardService.getWeekCascadas(plantId, weekDate, status)
        );
    }

    @GetMapping("/weekly-view")
    public ResponseEntity<StandardWeeklyResponseDTO> getWeeklyView(
            @RequestParam Long plantId,
            @RequestParam("weekDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekDate,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(
                cascadaStandardService.getStandardWeeklyView(plantId, weekDate, status)
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<List<CascadaSummaryDTO>> getCascadaSummaries(
            @RequestParam String status,
            @RequestParam(required = false) Long plantId,
            @RequestParam(name = "weekDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekDate,
            @RequestParam(required = false) Long recipientUserId,
            Authentication authentication
    ) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return ResponseEntity.status(403).build();
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMINISTRADOR".equals(a.getAuthority()));
        Long effectiveRecipientId = isAdmin && recipientUserId != null
                ? recipientUserId
                : principal.getUserId();
        return ResponseEntity.ok(
                cascadaStandardService.getCascadaStandardSummaries(status, plantId, weekDate, effectiveRecipientId)
        );
    }
}
