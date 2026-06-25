package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_standard;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_common.CascadaSummaryDTO;

import java.time.LocalDate;
import java.util.List;

public interface CascadaStandardService {
    CascadaResponseDTO getCascada(Long plantId, LocalDate weekStartDate, String shiftId, String dayKey, String status);

    void saveCascada(CascadaSaveRequestDTO request);

    CascadaStandardManualRowDTO createManualRow(CreateCascadaStandardManualRowRequestDTO request, Long userId);

    CascadaStandardManualRowDTO updateManualRow(Long manualStandardRowId, UpdateCascadaStandardManualRowRequestDTO request, Long userId);

    void deleteManualRow(Long manualStandardRowId);

    void updateCascadaStatus(
            Long plantId,
            LocalDate weekDate,
            LocalDate weekStartDate,
            LocalDate weekEndDate,
            Integer weekNumber,
            String shiftId,
            String dayKey,
            String status,
            Long userId,
            List<Long> recipientUserIds
    );

    CascadaWeekResponseDTO getWeekCascadas(Long plantId, LocalDate weekStartDate, String status);

    StandardWeeklyResponseDTO getStandardWeeklyView(Long plantId, LocalDate weekStartDate, String status);

    java.util.List<CascadaSummaryDTO> getCascadaStandardSummaries(
            String status,
            Long plantId,
            LocalDate weekStartDate,
            Long recipientUserId
    );
}
