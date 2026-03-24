package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CreateFlexsurManualRowRequestDTO;
import com.example.backend_sistema_LPE.dto.FlexsurManualRowDTO;
import com.example.backend_sistema_LPE.dto.FlexsurWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.FlexsurWeekSchemaDTO;
import com.example.backend_sistema_LPE.dto.FlexsurWeekSaveRequestDTO;
import com.example.backend_sistema_LPE.dto.UpdateFlexsurManualRowRequestDTO;

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

    java.util.List<com.example.backend_sistema_LPE.dto.CascadaSummaryDTO> getFlexsurSummaries(
            String status,
            Long plantId,
            LocalDate weekDate,
            Long recipientUserId
    );
}
