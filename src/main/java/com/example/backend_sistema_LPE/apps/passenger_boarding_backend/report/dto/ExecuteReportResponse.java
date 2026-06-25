package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report.dto;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report.enums.ReportExecutionStatus;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ExecuteReportResponse {
    private Long reportExecutionId;
    private ReportExecutionStatus status;
    private String sidUsed;
    private int rowCount;
    private int totalRows;
    private int persistedEvents;
    private int candidateRows;
    private int skippedNoCellsArray;
    private int skippedMissingRequiredColumns;
    private int skippedMissingPassengerId;
    private int skippedMissingUnitWialonId;
    private int skippedDuplicateWialonRowKey;
    private boolean cacheHit;
    private boolean alreadyRunning;
    private String infoMessage;
    private int durationMs;
    private Timestamp executedAt;
    private Timestamp finishedAt;
    private String errorMessage;
    private JsonNode execReportRaw;
    private JsonNode getResultRowsRaw;
    private JsonNode rowsRaw;
}
