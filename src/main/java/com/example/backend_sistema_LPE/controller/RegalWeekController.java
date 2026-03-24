package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.dto.CascadaStatusUpdateRequestDTO;
import com.example.backend_sistema_LPE.dto.CascadaSummaryDTO;
import com.example.backend_sistema_LPE.dto.CreateRegalManualRowRequestDTO;
import com.example.backend_sistema_LPE.dto.RegalManualRowDTO;
import com.example.backend_sistema_LPE.dto.RegalWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.RegalWeekSchemaDTO;
import com.example.backend_sistema_LPE.dto.RegalWeekSaveRequestDTO;
import com.example.backend_sistema_LPE.dto.UpdateRegalManualRowRequestDTO;
import com.example.backend_sistema_LPE.security.UserPrincipal;
import com.example.backend_sistema_LPE.service.RegalWeekService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/regal-week")
public class RegalWeekController {
    private final RegalWeekService regalWeekService;

    public RegalWeekController(RegalWeekService regalWeekService) {
        this.regalWeekService = regalWeekService;
    }

    @GetMapping("/schema")
    public ResponseEntity<RegalWeekSchemaDTO> getRegalWeekSchema(@RequestParam Long plantId) {
        return ResponseEntity.ok(regalWeekService.getRegalWeekSchema(plantId));
    }

    @GetMapping
    public ResponseEntity<RegalWeekResponseDTO> getRegalWeek(
            @RequestParam Long plantId,
            @RequestParam("weekDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekDate,
            @RequestParam(required = false) Long shiftId
    ) {
        return ResponseEntity.ok(regalWeekService.getRegalWeek(plantId, weekDate, shiftId));
    }

    @PutMapping
    public ResponseEntity<Void> saveRegalWeek(
            @RequestBody RegalWeekSaveRequestDTO request,
            Authentication authentication
    ) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getUserId();
        }
        regalWeekService.saveRegalWeek(request, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/manual-rows")
    public ResponseEntity<RegalManualRowDTO> createManualRow(
            @RequestBody CreateRegalManualRowRequestDTO request,
            Authentication authentication
    ) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getUserId();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(regalWeekService.createManualRow(request, userId));
    }

    @PatchMapping("/manual-rows/{manualRegalRowId}")
    public ResponseEntity<RegalManualRowDTO> updateManualRow(
            @PathVariable Long manualRegalRowId,
            @RequestBody UpdateRegalManualRowRequestDTO request,
            Authentication authentication
    ) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getUserId();
        }
        return ResponseEntity.ok(regalWeekService.updateManualRow(manualRegalRowId, request, userId));
    }

    @DeleteMapping("/manual-rows/{manualRegalRowId}")
    public ResponseEntity<Void> deleteManualRow(@PathVariable Long manualRegalRowId) {
        regalWeekService.deleteManualRow(manualRegalRowId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{plantId}/status")
    public ResponseEntity<Void> updateRegalStatus(
            @PathVariable Long plantId,
            @RequestBody CascadaStatusUpdateRequestDTO request,
            Authentication authentication
    ) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getUserId();
        }
        regalWeekService.updateRegalStatus(
                plantId,
                request.getWeekDate(),
                request.getStatus(),
                userId,
                request.getRecipientUserIds()
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<List<CascadaSummaryDTO>> getRegalSummaries(
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
        return ResponseEntity.ok(regalWeekService.getRegalSummaries(status, plantId, weekDate, effectiveRecipientId));
    }
}
