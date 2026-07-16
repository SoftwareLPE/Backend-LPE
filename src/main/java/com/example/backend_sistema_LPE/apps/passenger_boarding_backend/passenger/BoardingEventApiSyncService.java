package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger.dto.BoardingEventIngestionSummary;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report.ReportExecution;
import com.fasterxml.jackson.databind.JsonNode;

public interface BoardingEventApiSyncService {

    BoardingEventIngestionSummary ingest(
            ReportExecution reportExecution,
            JsonNode execResponse,
            JsonNode rowsResponse,
            Long resourceId,
            Long objectSecId,
            int tableIndex
    );
}
