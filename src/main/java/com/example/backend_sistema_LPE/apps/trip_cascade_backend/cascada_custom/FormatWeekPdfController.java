package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom;

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
@RequestMapping("/format-week")
public class FormatWeekPdfController {
    private final FormatWeekPdfService formatWeekPdfService;

    public FormatWeekPdfController(FormatWeekPdfService formatWeekPdfService) {
        this.formatWeekPdfService = formatWeekPdfService;
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> downloadCustomPdf(
            @RequestParam Long plantId,
            @RequestParam("weekDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekDate
    ) {
        byte[] pdfBytes = formatWeekPdfService.buildCustomWeeklyPdf(plantId, weekDate);
        String filename = "Cascada_Custom_" + plantId + "_" + weekDate + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdfBytes);
    }
}
