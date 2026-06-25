package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_standard;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/cascadas/week")
public class CascadaPdfController {
    private final CascadaPdfService cascadaPdfService;

    public CascadaPdfController(CascadaPdfService cascadaPdfService) {
        this.cascadaPdfService = cascadaPdfService;
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @RequestParam Long plantId,
            @RequestParam("weekDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean activeDrivers
    ) {
        String effectiveStatus = (status == null || status.isBlank()) ? "SENT" : status;
        byte[] pdfBytes = cascadaPdfService.buildWeeklyPdf(plantId, weekDate, effectiveStatus, activeDrivers);
        String filename = "Cascada_Viajes_" + plantId + "_" + weekDate + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdfBytes);
    }
}
