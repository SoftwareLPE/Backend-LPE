package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.dto.CascadaStatusUpdateRequestDTO;
import com.example.backend_sistema_LPE.dto.CascadaSummaryDTO;
import com.example.backend_sistema_LPE.dto.CreateFlexsurManualRowRequestDTO;
import com.example.backend_sistema_LPE.dto.FlexsurManualRowDTO;
import com.example.backend_sistema_LPE.dto.FlexsurWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.FlexsurWeekSchemaDTO;
import com.example.backend_sistema_LPE.dto.FlexsurWeekSaveRequestDTO;
import com.example.backend_sistema_LPE.dto.UpdateFlexsurManualRowRequestDTO;
import com.example.backend_sistema_LPE.security.UserPrincipal;
import com.example.backend_sistema_LPE.service.FlexsurWeekService;
import com.example.backend_sistema_LPE.service.InboxMessageUserStateService;
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
@RequestMapping("/flexsur-week")
public class FlexsurWeekController {
    private final FlexsurWeekService flexsurWeekService;
    private final InboxMessageUserStateService inboxMessageUserStateService;

    public FlexsurWeekController(
            FlexsurWeekService flexsurWeekService,
            InboxMessageUserStateService inboxMessageUserStateService
    ) {
        this.flexsurWeekService = flexsurWeekService;
        this.inboxMessageUserStateService = inboxMessageUserStateService;
    }

    @GetMapping("/schema")
    public ResponseEntity<FlexsurWeekSchemaDTO> getFlexsurWeekSchema(
            @RequestParam Long plantId,
            @RequestParam("weekDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekDate
    ) {
        return ResponseEntity.ok(flexsurWeekService.getFlexsurWeekSchema(plantId, weekDate));
    }

    @GetMapping
    public ResponseEntity<FlexsurWeekResponseDTO> getFlexsurWeek(
            @RequestParam Long plantId,
            @RequestParam("weekDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekDate,
            @RequestParam(required = false) Long shiftId
    ) {
        return ResponseEntity.ok(flexsurWeekService.getFlexsurWeek(plantId, weekDate, shiftId));
    }

    @PutMapping
    public ResponseEntity<Void> saveFlexsurWeek(
            @RequestBody FlexsurWeekSaveRequestDTO request,
            Authentication authentication
    ) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getUserId();
        }
        flexsurWeekService.saveFlexsurWeek(request, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/manual-rows")
    public ResponseEntity<FlexsurManualRowDTO> createManualRow(
            @RequestBody CreateFlexsurManualRowRequestDTO request,
            Authentication authentication
    ) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getUserId();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(flexsurWeekService.createManualRow(request, userId));
    }

    @PatchMapping("/manual-rows/{manualFlexsurRowId}")
    public ResponseEntity<FlexsurManualRowDTO> updateManualRow(
            @PathVariable Long manualFlexsurRowId,
            @RequestBody UpdateFlexsurManualRowRequestDTO request,
            Authentication authentication
    ) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getUserId();
        }
        return ResponseEntity.ok(flexsurWeekService.updateManualRow(manualFlexsurRowId, request, userId));
    }

    @DeleteMapping("/manual-rows/{manualFlexsurRowId}")
    public ResponseEntity<Void> deleteManualRow(@PathVariable Long manualFlexsurRowId) {
        flexsurWeekService.deleteManualRow(manualFlexsurRowId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{plantId}/status")
    public ResponseEntity<Void> updateFlexsurStatus(
            @PathVariable Long plantId,
            @RequestBody CascadaStatusUpdateRequestDTO request,
            Authentication authentication
    ) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getUserId();
        }
        flexsurWeekService.updateFlexsurStatus(
                plantId,
                request.getWeekDate(),
                parseShiftId(request.getShiftId()),
                request.getStatus(),
                userId,
                request.getRecipientUserIds()
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<List<CascadaSummaryDTO>> getFlexsurSummaries(
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
        List<CascadaSummaryDTO> summaries = flexsurWeekService.getFlexsurSummaries(
                status,
                plantId,
                weekDate,
                effectiveRecipientId
        );
        inboxMessageUserStateService.applyVisualStatus(summaries, effectiveRecipientId);
        return ResponseEntity.ok(summaries);
    }

    private Long parseShiftId(String shiftId) {
        if (shiftId == null || shiftId.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(shiftId);
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Invalid shiftId");
        }
    }
}
