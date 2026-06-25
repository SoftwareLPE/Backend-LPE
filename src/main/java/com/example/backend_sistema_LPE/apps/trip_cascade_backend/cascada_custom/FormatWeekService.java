package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_common.CascadaSummaryDTO;

public interface FormatWeekService {
    FormatWeekResponseDTO getFormatWeek(Long plantId, Long formatTypeId, java.time.LocalDate weekDate, Long shiftId);

    void saveFormatWeek(FormatWeekSaveRequestDTO request, Long userId);

    FormatWeekManualRowDTO createManualRow(CreateFormatWeekManualRowRequestDTO request, Long userId);

    FormatWeekManualRowDTO updateManualRow(Long manualRowId, UpdateFormatWeekManualRowRequestDTO request, Long userId);

    void deleteManualRow(Long manualRowId);

    void updateFormatWeekStatus(
            Long plantId,
            Long formatTypeId,
            java.time.LocalDate weekDate,
            java.time.LocalDate weekStartDate,
            java.time.LocalDate weekEndDate,
            Integer weekNumber,
            Long shiftId,
            String dayKey,
            String status,
            Long userId,
            java.util.List<Long> recipientUserIds
    );

    java.util.List<CascadaSummaryDTO> getFormatWeekSummaries(
            String status,
            Long plantId,
            java.time.LocalDate weekDate,
            Long recipientUserId
    );

    FormatWeekSchemaDTO getFormatWeekSchema(Long formatTypeId);
}
