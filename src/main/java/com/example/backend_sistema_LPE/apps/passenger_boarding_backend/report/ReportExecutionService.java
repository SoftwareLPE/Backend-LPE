package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report.dto.ExecuteReportRequest;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report.dto.ExecuteReportResponse;

public interface ReportExecutionService {
    ExecuteReportResponse executeReport(ExecuteReportRequest request);
}
