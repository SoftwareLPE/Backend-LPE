package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report.dto.ExecuteReportRequest;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report.dto.ExecuteReportResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportExecutionController {

    private final ReportExecutionService reportExecutionService;

    public ReportExecutionController(ReportExecutionService reportExecutionService) {
        this.reportExecutionService = reportExecutionService;
    }

    @PostMapping("/execute")
    public ResponseEntity<ExecuteReportResponse> executeReport(@Valid @RequestBody ExecuteReportRequest request) {
        ExecuteReportResponse response = reportExecutionService.executeReport(request);
        return ResponseEntity.ok(response);
    }
}
