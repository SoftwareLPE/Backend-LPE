package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.service.FlexsurWeekPdfService;
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
@RequestMapping("/flexsur-week")
public class FlexsurWeekPdfController {
    private final FlexsurWeekPdfService flexsurWeekPdfService;

    public FlexsurWeekPdfController(FlexsurWeekPdfService flexsurWeekPdfService) {
        this.flexsurWeekPdfService = flexsurWeekPdfService;
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> downloadFlexsurPdf(
            @RequestParam Long plantId,
            @RequestParam("weekDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekDate,
            @RequestParam(required = false) Long shiftId
    ) {
        byte[] pdfBytes = flexsurWeekPdfService.buildWeeklyPdf(plantId, weekDate, shiftId);
        String filename = shiftId == null
                ? "Cascada_Flexsur_" + plantId + "_" + weekDate + ".pdf"
                : "Cascada_Flexsur_" + plantId + "_" + weekDate + "_shift_" + shiftId + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdfBytes);
    }
}
