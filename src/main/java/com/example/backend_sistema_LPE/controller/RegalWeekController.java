package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.dto.CascadaStatusUpdateRequestDTO;
import com.example.backend_sistema_LPE.dto.CascadaSummaryDTO;
import com.example.backend_sistema_LPE.dto.RegalWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.RegalWeekSaveRequestDTO;
import com.example.backend_sistema_LPE.security.UserPrincipal;
import com.example.backend_sistema_LPE.service.RegalWeekService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
                userId
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<List<CascadaSummaryDTO>> getRegalSummaries(
            @RequestParam String status,
            @RequestParam(required = false) Long plantId,
            @RequestParam(name = "weekDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekDate
    ) {
        return ResponseEntity.ok(regalWeekService.getRegalSummaries(status, plantId, weekDate));
    }
}
