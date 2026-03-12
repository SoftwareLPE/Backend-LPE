package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CascadaResponseDTO;
import com.example.backend_sistema_LPE.dto.CascadaSaveRequestDTO;
import com.example.backend_sistema_LPE.dto.CascadaStandardManualRowDTO;
import com.example.backend_sistema_LPE.dto.CascadaWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.CreateCascadaStandardManualRowRequestDTO;
import com.example.backend_sistema_LPE.dto.StandardWeeklyResponseDTO;
import com.example.backend_sistema_LPE.dto.UpdateCascadaStandardManualRowRequestDTO;

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
            LocalDate weekStartDate,
            String shiftId,
            String dayKey,
            String status,
            Long userId,
            List<Long> recipientUserIds
    );

    CascadaWeekResponseDTO getWeekCascadas(Long plantId, LocalDate weekStartDate, String status);

    StandardWeeklyResponseDTO getStandardWeeklyView(Long plantId, LocalDate weekStartDate, String status);

    java.util.List<com.example.backend_sistema_LPE.dto.CascadaSummaryDTO> getCascadaStandardSummaries(
            String status,
            Long plantId,
            LocalDate weekStartDate,
            Long recipientUserId
    );
}
