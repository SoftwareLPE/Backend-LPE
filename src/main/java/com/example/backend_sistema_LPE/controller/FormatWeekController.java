package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.dto.CascadaSummaryDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekSaveRequestDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekSchemaDTO;
import com.example.backend_sistema_LPE.dto.CreateFormatWeekManualRowRequestDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekManualRowDTO;
import com.example.backend_sistema_LPE.dto.UpdateFormatWeekManualRowRequestDTO;
import com.example.backend_sistema_LPE.dto.CascadaStatusUpdateRequestDTO;
import com.example.backend_sistema_LPE.security.UserPrincipal;
import com.example.backend_sistema_LPE.service.FormatWeekService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/format-week")
public class FormatWeekController {
    private final FormatWeekService formatWeekService;

    public FormatWeekController(FormatWeekService formatWeekService) {
        this.formatWeekService = formatWeekService;
    }

    @GetMapping
    public ResponseEntity<FormatWeekResponseDTO> getFormatWeek(
            @RequestParam Long plantId,
            @RequestParam Long formatTypeId,
            @RequestParam("weekDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekDate,
            @RequestParam(required = false) Long shiftId
    ) {
        return ResponseEntity.ok(
                formatWeekService.getFormatWeek(plantId, formatTypeId, weekDate, shiftId)
        );
    }

    @PutMapping
    public ResponseEntity<Void> saveFormatWeek(
            @RequestBody FormatWeekSaveRequestDTO request,
            Authentication authentication
    ) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getUserId();
        }
        formatWeekService.saveFormatWeek(request, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/manual-rows")
    public ResponseEntity<FormatWeekManualRowDTO> createManualRow(
            @RequestBody CreateFormatWeekManualRowRequestDTO request,
            Authentication authentication
    ) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getUserId();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(formatWeekService.createManualRow(request, userId));
    }

    @PatchMapping("/manual-rows/{manualRowId}")
    public ResponseEntity<FormatWeekManualRowDTO> updateManualRow(
            @PathVariable Long manualRowId,
            @RequestBody UpdateFormatWeekManualRowRequestDTO request,
            Authentication authentication
    ) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getUserId();
        }
        return ResponseEntity.ok(formatWeekService.updateManualRow(manualRowId, request, userId));
    }

    @DeleteMapping("/manual-rows/{manualRowId}")
    public ResponseEntity<Void> deleteManualRow(@PathVariable Long manualRowId) {
        formatWeekService.deleteManualRow(manualRowId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{plantId}/status")
    public ResponseEntity<Void> updateStatus(
            @org.springframework.web.bind.annotation.PathVariable Long plantId,
            @RequestBody CascadaStatusUpdateRequestDTO request,
            Authentication authentication
    ) {
        if (request.getFormatTypeId() == null) {
            throw new RuntimeException("formatTypeId is required");
        }
        if (request.getShiftId() == null || request.getShiftId().isBlank()) {
            throw new RuntimeException("shiftId is required");
        }
        Long shiftId;
        try {
            shiftId = Long.parseLong(request.getShiftId());
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Invalid shiftId");
        }
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getUserId();
        }
        formatWeekService.updateFormatWeekStatus(
                plantId,
                request.getFormatTypeId(),
                request.getWeekDate(),
                shiftId,
                request.getDayKey(),
                request.getStatus(),
                userId,
                request.getRecipientUserIds()
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<List<CascadaSummaryDTO>> getFormatWeekSummaries(
            @RequestParam String status,
            @RequestParam(required = false) Long plantId,
            @RequestParam(name = "weekDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekDate,
            @RequestParam(required = false) Long recipientUserId,
            Authentication authentication
    ) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMINISTRADOR".equals(a.getAuthority()));
        Long effectiveRecipientId = recipientUserId;
        if (!isAdmin) {
            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
                effectiveRecipientId = principal.getUserId();
            } else {
                return ResponseEntity.status(403).build();
            }
        }
        return ResponseEntity.ok(
                formatWeekService.getFormatWeekSummaries(status, plantId, weekDate, effectiveRecipientId)
        );
    }

    @GetMapping("/schema")
    public ResponseEntity<FormatWeekSchemaDTO> getFormatWeekSchema(
            @RequestParam Long formatTypeId
    ) {
        return ResponseEntity.ok(formatWeekService.getFormatWeekSchema(formatTypeId));
    }
}
