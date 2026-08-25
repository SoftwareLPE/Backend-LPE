package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports/boarding-events/export")
public class BoardingEventPdfExportController {

    private final BoardingEventPdfExportService boardingEventPdfExportService;

    public BoardingEventPdfExportController(BoardingEventPdfExportService boardingEventPdfExportService) {
        this.boardingEventPdfExportService = boardingEventPdfExportService;
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam Long plantId,
            @RequestParam Long from,
            @RequestParam Long to,
            @RequestParam(required = false) Long resolvedShiftId,
            @RequestParam(required = false) String windowType
    ) {
        byte[] pdf = boardingEventPdfExportService.exportPlantBoardingEventsPdf(
                plantId,
                from,
                to,
                resolvedShiftId,
                windowType
        );

        String filename = "reporte-abordajes-" + plantId + "-" + from + "-" + to + ".pdf";

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString()
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
