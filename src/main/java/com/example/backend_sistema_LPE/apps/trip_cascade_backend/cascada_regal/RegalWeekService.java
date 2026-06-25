package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_regal;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_common.CascadaSummaryDTO;

import java.time.LocalDate;

public interface RegalWeekService {
    RegalWeekSchemaDTO getRegalWeekSchema(Long plantId);

    RegalWeekResponseDTO getRegalWeek(Long plantId, LocalDate weekDate, Long shiftId);

    void saveRegalWeek(RegalWeekSaveRequestDTO request, Long userId);

    RegalManualRowDTO createManualRow(CreateRegalManualRowRequestDTO request, Long userId);

    RegalManualRowDTO updateManualRow(Long manualRegalRowId, UpdateRegalManualRowRequestDTO request, Long userId);

    void deleteManualRow(Long manualRegalRowId);

    void updateRegalStatus(Long plantId, LocalDate weekDate, String status, Long userId);
    
    void updateRegalStatus(
            Long plantId,
            LocalDate weekDate,
            String status,
            Long userId,
            java.util.List<Long> recipientUserIds
    );

    java.util.List<CascadaSummaryDTO> getRegalSummaries(
            String status,
            Long plantId,
            LocalDate weekDate,
            Long recipientUserId
    );
}
