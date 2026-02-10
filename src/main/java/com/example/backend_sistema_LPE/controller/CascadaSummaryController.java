package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.dto.CascadaSummaryDTO;
import com.example.backend_sistema_LPE.service.CascadaService;
import com.example.backend_sistema_LPE.security.UserPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/cascadas")
public class CascadaSummaryController {
    private final CascadaService cascadaService;

    public CascadaSummaryController(CascadaService cascadaService) {
        this.cascadaService = cascadaService;
    }

    @GetMapping
    public ResponseEntity<List<CascadaSummaryDTO>> getCascadaSummaries(
            @RequestParam String status,
            @RequestParam(required = false) Long plantId,
            @RequestParam(name = "weekDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
            @RequestParam(required = false) Long recipientUserId,
            Authentication authentication
    ) {
        // Inbox listing for sent cascadas.
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
                cascadaService.getCascadaSummaries(status, plantId, weekStartDate, effectiveRecipientId)
        );
    }
}
