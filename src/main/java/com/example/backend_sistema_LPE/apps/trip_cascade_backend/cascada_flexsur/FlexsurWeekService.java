package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_flexsur;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_common.CascadaSummaryDTO;

import java.time.LocalDate;

public interface FlexsurWeekService {
    FlexsurWeekSchemaDTO getFlexsurWeekSchema(Long plantId, LocalDate weekDate);

    FlexsurWeekResponseDTO getFlexsurWeek(Long plantId, LocalDate weekDate, Long shiftId);

    void saveFlexsurWeek(FlexsurWeekSaveRequestDTO request, Long userId);

    FlexsurManualRowDTO createManualRow(CreateFlexsurManualRowRequestDTO request, Long userId);

    FlexsurManualRowDTO updateManualRow(Long manualFlexsurRowId, UpdateFlexsurManualRowRequestDTO request, Long userId);

    void deleteManualRow(Long manualFlexsurRowId);

    void updateFlexsurStatus(Long plantId, LocalDate weekDate, Long shiftId, String status, Long userId);

    void updateFlexsurStatus(
            Long plantId,
            LocalDate weekDate,
            Long shiftId,
            String status,
            Long userId,
            java.util.List<Long> recipientUserIds
    );

    java.util.List<CascadaSummaryDTO> getFlexsurSummaries(
            String status,
            Long plantId,
            LocalDate weekDate,
            Long recipientUserId
    );
}
