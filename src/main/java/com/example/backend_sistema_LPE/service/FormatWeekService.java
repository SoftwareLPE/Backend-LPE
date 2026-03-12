package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.FormatWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekSaveRequestDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekSchemaDTO;
import com.example.backend_sistema_LPE.dto.CascadaSummaryDTO;
import com.example.backend_sistema_LPE.dto.CreateFormatWeekManualRowRequestDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekManualRowDTO;
import com.example.backend_sistema_LPE.dto.UpdateFormatWeekManualRowRequestDTO;

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
