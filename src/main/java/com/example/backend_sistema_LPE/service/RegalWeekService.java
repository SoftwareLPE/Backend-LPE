package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CreateRegalManualRowRequestDTO;
import com.example.backend_sistema_LPE.dto.RegalManualRowDTO;
import com.example.backend_sistema_LPE.dto.RegalWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.RegalWeekSchemaDTO;
import com.example.backend_sistema_LPE.dto.RegalWeekSaveRequestDTO;
import com.example.backend_sistema_LPE.dto.UpdateRegalManualRowRequestDTO;

import java.time.LocalDate;

public interface RegalWeekService {
    RegalWeekSchemaDTO getRegalWeekSchema(Long plantId);

    RegalWeekResponseDTO getRegalWeek(Long plantId, LocalDate weekDate, Long shiftId);

    void saveRegalWeek(RegalWeekSaveRequestDTO request, Long userId);

    RegalManualRowDTO createManualRow(CreateRegalManualRowRequestDTO request, Long userId);

    RegalManualRowDTO updateManualRow(Long manualRegalRowId, UpdateRegalManualRowRequestDTO request, Long userId);

    void deleteManualRow(Long manualRegalRowId);

    void updateRegalStatus(Long plantId, LocalDate weekDate, String status, Long userId);

    java.util.List<com.example.backend_sistema_LPE.dto.CascadaSummaryDTO> getRegalSummaries(
            String status,
            Long plantId,
            LocalDate weekDate
    );
}
