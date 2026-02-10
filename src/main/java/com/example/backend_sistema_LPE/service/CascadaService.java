package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CascadaResponseDTO;
import com.example.backend_sistema_LPE.dto.CascadaSaveRequestDTO;
import com.example.backend_sistema_LPE.dto.CascadaWeekResponseDTO;

import java.time.LocalDate;

public interface CascadaService {
    CascadaResponseDTO getCascada(Long plantId, LocalDate weekStartDate, String shiftId, String dayKey, String status);

    void saveCascada(CascadaSaveRequestDTO request);

    void sendCascada(
            Long plantId,
            LocalDate weekStartDate,
            String shiftId,
            String dayKey,
            Long userId,
            java.util.List<Long> recipientUserIds
    );

    void deleteCascada(
            Long plantId,
            LocalDate weekStartDate,
            String shiftId,
            String dayKey,
            Long userId
    );

    void updateCascadaStatus(
            Long plantId,
            LocalDate weekStartDate,
            String shiftId,
            String dayKey,
            String status,
            Long userId,
            java.util.List<Long> recipientUserIds
    );

    CascadaWeekResponseDTO getWeekCascadas(Long plantId, LocalDate weekStartDate, String status);

    java.util.List<com.example.backend_sistema_LPE.dto.CascadaSummaryDTO> getCascadaSummaries(
            String status,
            Long plantId,
            LocalDate weekStartDate,
            Long recipientUserId
    );
}
