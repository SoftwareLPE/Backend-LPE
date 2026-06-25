package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_regal;

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
@RequestMapping("/regal-week")
public class RegalWeekPdfController {
    private final RegalWeekPdfService regalWeekPdfService;

    public RegalWeekPdfController(RegalWeekPdfService regalWeekPdfService) {
        this.regalWeekPdfService = regalWeekPdfService;
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> downloadRegalPdf(
            @RequestParam Long plantId,
            @RequestParam("weekDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekDate,
            @RequestParam(required = false) Long shiftId
    ) {
        byte[] pdfBytes = regalWeekPdfService.buildWeeklyPdf(plantId, weekDate, shiftId);
        String filename = "Cascada_Regal_" + plantId + "_" + weekDate + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdfBytes);
    }
}
